package com.ianford.tal.io;

import com.google.gson.Gson;
import com.ianford.podcasts.model.ParsedEpisode;
import com.ianford.podcasts.model.db.DBPartitionKey;
import com.ianford.podcasts.model.db.DBSortKey;
import com.ianford.podcasts.model.db.PodcastDBDBRecord;
import com.ianford.podcasts.model.jekyll.BlogEpisode;
import com.ianford.podcasts.model.jekyll.BlogEpisodeAct;
import com.ianford.podcasts.model.jekyll.BlogEpisodeContributor;
import com.ianford.podcasts.model.jekyll.BlogEpisodeStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RawEpisodeParser implements Function<Path, Optional<ParsedEpisode>> {

    private static final Logger logger = LogManager.getLogger();

    private static final String UNSPECIFIED_ROLE = "UNSPECIFIED_ROLE";
    private static final String UNSPECIFIED_NAME = "UNSPECIFIED_NAME";
    private static final String UNSPECIFIED_AIR_DATE = "UNSPECIFIED_AIR_DATE";

    private final Function<String, Document> docLoader;

    private final Gson gson;


    /**
     * Constructor
     *
     * @param docLoader Used to load files as JSoup documents
     * @param gson
     */
    public RawEpisodeParser(Function<String, Document> docLoader, Gson gson) {
        this.docLoader = docLoader;
        this.gson = gson;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<ParsedEpisode> apply(Path episodeFilePath) {
        final Document doc = docLoader.apply(episodeFilePath.toString());
        if (null == doc) {
            logger.info("Doc was empty at {}",
                    episodeFilePath.toString());
            return Optional.empty();
        }

        // Pull out primary page elements
        final Element contentDiv = doc.getElementById("content");
        final Element primaryArticle = contentDiv.getElementsByTag("article")
                .first();
        final Element header = primaryArticle.getElementsByTag("header")
                .first();
        final Element title = header.getElementsByTag("h1")
                .first();
        final String titleContent = title.text();

        // Get episode number and name
        final Pattern pattern = Pattern.compile("(\\d+):\\s(.*)");
        final Matcher matcher = pattern.matcher(titleContent);
        if (!matcher.matches()) {
            logger.info("Unable to find title content match, exiting");
            return Optional.empty();
        }

        // Episode name and number
        final int episodeNumber = Integer.parseInt(matcher.group(1));
        final String episodeName = matcher.group(2);

        // Identify acts
        final Element divContent = primaryArticle.getElementsByClass("content")
                .first();
        final Elements actElementList = divContent.getElementsByClass("act");

        // Create stores for data we'll be parsing out
        final List<PodcastDBDBRecord> podcastDBDBRecordList = new ArrayList<>();
        final Map<Integer, BlogEpisode> blogEpisodeMap = new HashMap<>();
        BlogEpisode blogEpisode = blogEpisodeMap.computeIfAbsent(episodeNumber,
                (epNum) -> new BlogEpisode(epNum));

        // Set title of episode
        blogEpisode.setEpisodeTitle(episodeName);

        // Record for title of episode
        PodcastDBDBRecord episodeTitleRecord = new PodcastDBDBRecord();
        episodeTitleRecord.setPrimaryKey(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber));
        episodeTitleRecord.setSort(DBSortKey.EPISODE_NAME.getValue());
        episodeTitleRecord.setValue(episodeName);
        podcastDBDBRecordList.add(episodeTitleRecord);


        // TODO: Add air-date record
        int actNumber = 1;
        int statementCountForEpisode = 1;
        int statementCountForContributor = 1;
        for (final Element actElement : actElementList) {

            // Make an act object if it doesn't exist already
            BlogEpisodeAct blogEpisodeAct = blogEpisode.getActMap()
                    .computeIfAbsent(actNumber,
                            (num) -> new BlogEpisodeAct(num));

            // Set name of act
            final String actName = extractActName(actElement);
            blogEpisodeAct.setActName(actName);

            // Create DB record for act name
            PodcastDBDBRecord actNameRecord = new PodcastDBDBRecord();
            actNameRecord.setPrimaryKey(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber));
            actNameRecord.setSort(DBSortKey.ACT_NAME.format(actNumber));
            actNameRecord.setValue(actName);
            podcastDBDBRecordList.add(actNameRecord);

            // Build records for statements made in act
            int statementCountForAct = 1;
            for (final Element statementElement : extractActStatements(actElement)) {

                final String speakerRole = extractSpeakerRole(statementElement);
                final String speakerName = extractSpeakerName(statementElement);
                for (final Element paragraph : extractSpeakerStatements(statementElement)) {
                    final String startTime = extractStartTime(paragraph);
                    final String paragraphText = paragraph.text();

                    String partitionKey = DBPartitionKey.EPISODE_NUMBER.format(episodeNumber);
                    BlogEpisodeStatement blogEpisodeStatement = new BlogEpisodeStatement(speakerName,
                            paragraphText);

                    // Add statement to current act
                    blogEpisodeAct.getStatementList()
                            .add(blogEpisodeStatement);
                    blogEpisode.getStatementList()
                            .add(blogEpisodeStatement);

                    // Build up contributor
                    BlogEpisodeContributor blogEpisodeContributor = blogEpisodeAct.getContributorMap()
                            .computeIfAbsent(blogEpisodeStatement.getSpeakerName(),
                                    (missingSpeakerName) -> new BlogEpisodeContributor(missingSpeakerName));

                    // Add statement to this contributor
                    blogEpisodeContributor.getStatements()
                            .add(paragraphText);

                    // Add spoken words to contributor for this statement
                    blogEpisodeContributor.getSpokenWords()
                            .addAll(Arrays.stream(paragraphText.split("\\W+"))
                                    .map(String::toString)
                                    .collect(Collectors.toSet()));

                    // Add episode title to contributor episodes list
                    blogEpisodeContributor.getEpisodes()
                            .computeIfAbsent(episodeNumber,
                                    (epNum) -> blogEpisode.getEpisodeTitle());

                    // Add contributor to episode record
                    blogEpisode.getContributorMap()
                            .computeIfAbsent(blogEpisodeStatement.getSpeakerName(),
                                    (missingSpeakerName) -> blogEpisodeContributor);

                    // Serialize statement for DB
                    String serializedStatement = gson.toJson(blogEpisodeStatement);

                    // Create db record for act statement
                    PodcastDBDBRecord actStatementRecord = new PodcastDBDBRecord();
                    actStatementRecord.setPrimaryKey(partitionKey);
                    actStatementRecord.setSort(DBSortKey.ACT_STATEMENT.format(actNumber,
                            statementCountForAct,
                            startTime));
                    actStatementRecord.setValue(serializedStatement);
                    podcastDBDBRecordList.add(actStatementRecord);
                    statementCountForAct++;

                    // Create db record for episode statement
                    PodcastDBDBRecord episodeStatementRecord = new PodcastDBDBRecord();
                    episodeStatementRecord.setPrimaryKey(partitionKey);
                    episodeStatementRecord.setSort(DBSortKey.EPISODE_STATEMENT.format(statementCountForEpisode,
                            startTime));
                    episodeStatementRecord.setValue(serializedStatement);
                    podcastDBDBRecordList.add(episodeStatementRecord);
                    statementCountForEpisode++;

                    // Create db record for contributor statement
                    PodcastDBDBRecord contributorStatementRecord = new PodcastDBDBRecord();
                    contributorStatementRecord.setPrimaryKey(DBPartitionKey.CONTRIBUTOR.format(blogEpisodeStatement.getSpeakerName()));
                    contributorStatementRecord.setSort(DBSortKey.CONTRIBUTOR_STATEMENT.format(
                            episodeNumber,
                            actNumber,
                            statementCountForContributor,
                            startTime));
                    contributorStatementRecord.setValue(blogEpisodeStatement.getText());
                    podcastDBDBRecordList.add(contributorStatementRecord);
                    statementCountForContributor++;
                }
            }
            actNumber++;
        }
        return Optional.of(new ParsedEpisode(podcastDBDBRecordList,
                blogEpisodeMap));
    }

    /**
     * Reads the act name from the provided element
     *
     * @param element The element to inspect
     * @return The act name
     */
    private String extractActName(final Element element) {
        final Elements actNameWrapper = element.getElementsByTag("h3");
        return actNameWrapper.isEmpty() ?
                null :
                actNameWrapper.first()
                        .text();
    }

    /**
     * Extracts a list of individual statements from the provided element defining the act to parse
     *
     * @param element The element to inspect
     * @return Elements containing act statements
     */
    private Elements extractActStatements(final Element element) {
        final Element actInner = element.getElementsByClass("act-inner")
                .first();
        return actInner.children();
    }

    /**
     * Extracts a list of speaker statements from the provided element defining the set of statements for this speaker
     *
     * @param element The element to inspect
     * @return Elements containing speaker statements
     */
    private Elements extractSpeakerStatements(final Element element) {
        return element.getElementsByTag("p");
    }

    /**
     * Extracts a start time from the provided element
     *
     * @param element The element to inspect
     * @return The start time
     */
    private String extractStartTime(final Element element) {
        return element.attr("begin");
    }

    /**
     * Extracts the speaker's role from the provided element
     *
     * @param element The element to inspect
     * @return The speaker role
     */
    private String extractSpeakerRole(final Element element) {
        return element.attr("class");
    }

    /**
     * Extracts the speaker's name from the provided element
     *
     * @param element The element to inspect
     * @return The speaker name
     */
    private String extractSpeakerName(final Element element) {
        final Elements speakerNameWrapper = element.getElementsByTag("h4");
        return speakerNameWrapper.isEmpty() ?
                null :
                speakerNameWrapper.first()
                        .text();
    }

}
