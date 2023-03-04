package com.ianford.podcasts.tal.io;

import com.ianford.podcasts.model.BasicEpisodeRecord;
import com.ianford.podcasts.model.DBKey;
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
public class RawEpisodeParser implements Function<Path, List<BasicEpisodeRecord>> {

    private static final Logger logger = LogManager.getLogger();

    private static final String UNSPECIFIED_ROLE = "UNSPECIFIED_ROLE";
    private static final String UNSPECIFIED_NAME = "UNSPECIFIED_NAME";
    private static final String UNSPECIFIED_AIR_DATE = "UNSPECIFIED_AIR_DATE";

    private final Function<String, Document> docLoader;

    /**
     * Constructor
     *
     * @param docLoader Used to load files as JSoup documents
     */
    public RawEpisodeParser(Function<String, Document> docLoader) {
        this.docLoader = docLoader;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BasicEpisodeRecord> apply(Path episodeFilePath) {
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
        final List<BasicEpisodeRecord> episodeRecordList = new ArrayList<>();


        BasicEpisodeRecord episodeTitleRecord = new BasicEpisodeRecord();
        episodeTitleRecord.setShowName(DBKey.PARTITION.getValue());
        episodeTitleRecord.setSort(DBKey.EPISODE_NAME.format(episodeNumber));
        episodeTitleRecord.setValue(episodeName);
        episodeRecordList.add(episodeTitleRecord);

        // TODO: Add air-date record

        for (final Element act : actList) {
            final String actName = extractActName(act);


            BasicEpisodeRecord actNameRecord = new BasicEpisodeRecord();
            actNameRecord.setShowName(DBKey.PARTITION.getValue());
            actNameRecord.setSort(DBKey.ACT_NAME.format(episodeNumber, actNumber));
            actNameRecord.setValue(actName);
            episodeRecordList.add(actNameRecord);

            for (final Element statement : extractActStatements(act)) {
                final String speakerRole = extractSpeakerRole(statement);
                final String speakerName = extractSpeakerName(statement);
                for (final Element paragraph : extractSpeakerStatements(statement)) {
                    final String startTime = extractStartTime(paragraph);
                    final String paragraphText = paragraph.text();

                    BasicEpisodeRecord speakerTextRecord = new BasicEpisodeRecord();
                    speakerTextRecord.setShowName(DBKey.PARTITION.getValue());
                    speakerTextRecord.setSort(
                            DBKey.SPEAKER_TEXT.format(episodeNumber, actNumber, startTime));
                    speakerTextRecord.setValue(paragraphText);
                    episodeRecordList.add(speakerTextRecord);

                    BasicEpisodeRecord speakerNameRecord = new BasicEpisodeRecord();
                    speakerNameRecord.setShowName(DBKey.PARTITION.getValue());
                    speakerNameRecord.setSort(
                            DBKey.SPEAKER_NAME.format(episodeNumber, actNumber, startTime));
                    speakerNameRecord.setValue(speakerName);
                    episodeRecordList.add(speakerNameRecord);
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
