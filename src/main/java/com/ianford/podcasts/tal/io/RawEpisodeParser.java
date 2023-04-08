package com.ianford.podcasts.tal.io;

import com.google.gson.Gson;
import com.ianford.podcasts.model.BasicPodcastRecord;
import com.ianford.podcasts.model.DBPartitionKey;
import com.ianford.podcasts.model.DBSortKey;
import com.ianford.podcasts.model.jekyll.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class RawEpisodeParser implements Function<Path, List<BasicPodcastRecord>> {

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
    public List<BasicPodcastRecord> apply(Path episodeFilePath) {
        final Document doc = docLoader.apply(episodeFilePath.toString());
        if (null == doc) {
            logger.info("Doc was empty at {}", episodeFilePath.toString());
            return Collections.EMPTY_LIST;
        }

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
            return Collections.EMPTY_LIST;
        }
        final int episodeNumber = Integer.parseInt(matcher.group(1));
        final String episodeName = matcher.group(2);

        final Element divContent = primaryArticle.getElementsByClass("content")
                .first();
        final Elements actList = divContent.getElementsByClass("act");

        // Iterate over acts and parse out relevant data. Generate records and store in a List for return.
        int actNumber = 1;
        int statementCountForEpisode = 1;
        final List<BasicPodcastRecord> episodeRecordList = new ArrayList<>();

        // Record for title of episode
        BasicPodcastRecord episodeTitleRecord = new BasicPodcastRecord();
        episodeTitleRecord.setPrimaryKey(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber));
        episodeTitleRecord.setSort(DBSortKey.EPISODE_NAME.getValue());
        episodeTitleRecord.setValue(episodeName);
        episodeRecordList.add(episodeTitleRecord);

        // TODO: Add air-date record
        for (final Element act : actList) {

            // Record for name of act
            final String actName = extractActName(act);
            BasicPodcastRecord actNameRecord = new BasicPodcastRecord();
            actNameRecord.setPrimaryKey(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber));
            actNameRecord.setSort(DBSortKey.ACT_NAME.format(actNumber));
            actNameRecord.setValue(actName);
            episodeRecordList.add(actNameRecord);

            // Build records for statements made in act
            int statementCountForAct = 1;
            for (final Element actStatement : extractActStatements(act)) {
                final String speakerRole = extractSpeakerRole(actStatement);
                final String speakerName = extractSpeakerName(actStatement);
                for (final Element paragraph : extractSpeakerStatements(actStatement)) {
                    final String startTime = extractStartTime(paragraph);
                    final String paragraphText = paragraph.text();

                    String partitionKey = DBPartitionKey.EPISODE_NUMBER.format(episodeNumber);
                    Statement statement = new Statement(speakerName, paragraphText);
                    String serializedStatement = gson.toJson(statement);

                    BasicPodcastRecord actStatementRecord = new BasicPodcastRecord();
                    actStatementRecord.setPrimaryKey(partitionKey);
                    actStatementRecord.setSort(DBSortKey.ACT_STATEMENT.format(actNumber, statementCountForAct, startTime));
                    actStatementRecord.setValue(serializedStatement);
                    episodeRecordList.add(actStatementRecord);
                    statementCountForAct++;

                    BasicPodcastRecord episodeStatementRecord = new BasicPodcastRecord();
                    episodeStatementRecord.setPrimaryKey(partitionKey);
                    episodeStatementRecord.setSort(DBSortKey.EPISODE_STATEMENT.format(statementCountForEpisode, startTime));
                    episodeStatementRecord.setValue(serializedStatement);
                    episodeRecordList.add(episodeStatementRecord);
                    statementCountForEpisode++;
                }
            }
            actNumber++;
        }
        return episodeRecordList;
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
