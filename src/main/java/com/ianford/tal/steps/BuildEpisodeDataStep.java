package com.ianford.tal.steps;

import com.google.gson.Gson;
import com.ianford.podcasts.model.ParsedEpisode;
import com.ianford.podcasts.model.db.DBPartitionKey;
import com.ianford.podcasts.model.db.DBSortKey;
import com.ianford.podcasts.model.db.PodcastDBDBRecord;
import com.ianford.podcasts.model.jekyll.BlogEpisode;
import com.ianford.podcasts.model.jekyll.BlogEpisodeAct;
import com.ianford.podcasts.model.jekyll.BlogEpisodeContributor;
import com.ianford.podcasts.model.jekyll.BlogEpisodeStatement;
import com.ianford.tal.io.RawEpisodeParser;
import com.ianford.tal.model.PipelineConfig;
import com.ianford.tal.util.DBUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Takes all existing raw data, parses it, and writes it to DDB and to our serialized representation used for Jekyll.
 */
public class BuildEpisodeDataStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();
    private final DBUtil dbUtil;
    private final RawEpisodeParser episodeParser;
    private final Gson gson;

    /**
     * Constructor.
     *
     * @param dbUtil        Used to store records we are backfilling.
     * @param episodeParser Used to convert raw HTML files into episode data.
     * @param gson          Used to serialize and deserialize statements
     */
    public BuildEpisodeDataStep(DBUtil dbUtil, RawEpisodeParser episodeParser, Gson gson) {
        this.dbUtil = dbUtil;
        this.episodeParser = episodeParser;
        this.gson = gson;
    }

    private static void extractActNameFromRecord(BlogEpisode blogEpisode, PodcastDBDBRecord episodeRecord) {
        Matcher actNameMatcher = DBSortKey.ACT_NAME.matcher(episodeRecord.getSort());
        if (!actNameMatcher.matches() || actNameMatcher.groupCount() != 1) {
            logger.info("Mismatched key: {}",
                    episodeRecord.getSort());
            return;
        }

        int actNumber = Integer.parseInt(actNameMatcher.group(1));

        Map<Integer, BlogEpisodeAct> actMap = blogEpisode.getActMap();
        BlogEpisodeAct blogEpisodeAct = actMap.computeIfAbsent(actNumber,
                (num) -> new BlogEpisodeAct(num));
        blogEpisodeAct.setActName(episodeRecord.getValue());
    }

    @Override
    public void run(PipelineConfig pipelineConfig) throws IOException {

        List<BlogEpisode.Summary> episodeSummaryList = new ArrayList<>();

        // Get every episode record up to this latest number
//        int latestEpNumber = getLatestEpNumber();


        int latestEpNumber = dbUtil.getMostRecentlyParsedEpisode()
                .orElseThrow();

        for (int episodeNumber = 1; episodeNumber <= latestEpNumber; episodeNumber++) {

            // Generate an output path for this episode and make sure it exists
            Path outputPath = pipelineConfig.buildPathForEpisode(episodeNumber);

            // Does this episode already exist? If so just load it and add it to our episode list.
            if (outputPath.toFile()
                    .exists()) {
                episodeSummaryList.add(gson.fromJson(Files.readString(outputPath),
                                BlogEpisode.class)
                        .summarize());
                continue;
            }


            // Create a new blogEpisode instance for this episode and set the title
            BlogEpisode blogEpisode = dbUtil.getEpisode(episodeNumber)
                    .orElseThrow();
//            BlogEpisode blogEpisode = new BlogEpisode(episodeNumber);
////            Optional<String> episodeTitle = dbUtil.getEpisodeTitle(episodeNumber);
////            if (episodeTitle.isEmpty()) {
////                continue;
////            }
////            blogEpisode.setEpisodeTitle(episodeTitle.get());
////
////
////            // Iterate over every statement in the episode and add them to this blog episode
////            Optional<List<BlogEpisodeStatement>> episodeStatements = dbUtil.getStatementsForEpisode(episodeNumber);
////            if (!episodeStatements.isPresent()) {
////                continue;
////            }
////            for (BlogEpisodeStatement episodeStatement : episodeStatements.get()) {
////                // Add statement to this episode
////                blogEpisode.getStatementList().add(episodeStatement);
////
////                // Add speaker to contributors
////                pipelineConfig.getContributors()
////                        .add(episodeStatement.getSpeakerName());
////
////                // Update episode contributors based on this statement
////                BlogEpisodeContributor blogEpisodeContributor =
////                        blogEpisode.getContributorMap().computeIfAbsent(episodeStatement.getSpeakerName(),
////                                (speakerName) -> new BlogEpisodeContributor(speakerName));
////                blogEpisodeContributor.getStatements()
////                        .add(episodeStatement.getText());
////                blogEpisodeContributor.getSpokenWords()
////                        .addAll(List.of(episodeStatement.getText()
////                                .split("\\W+")));
////                blogEpisodeContributor.getEpisodes()
////                        .computeIfAbsent(episodeNumber,
////                                (epNum) -> blogEpisode.getEpisodeTitle());
////            }
////
////
////            // Get every act statement for the current episode in our loop
////            QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
////                    .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
////                            .partitionValue(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber))
////                            .sortValue("ACT_")
////                            .build()))
////                    .build();
////            PageIterable<PodcastDBDBRecord> actQueryResults = table.query(queryEnhancedRequest);
////            SdkIterable<PodcastDBDBRecord> actQueryResultsIterable = actQueryResults.items();
////            // For every act statement in this episode....
////            for (PodcastDBDBRecord episodeRecord : actQueryResultsIterable) {
////
////                // We're only looking for act statements right now...right?
////                DBSortKey sortKey = DBSortKey.resolveKeyType(episodeRecord.getSort())
////                        .orElseThrow(() -> new RuntimeException("Could not match this sort key"));
////
////                if (DBSortKey.EPISODE_NAME.equals(sortKey)) {
////                    blogEpisode.setEpisodeTitle(episodeRecord.getValue());
////                } else if (DBSortKey.ACT_NAME.equals(sortKey)) {
////                    extractActNameFromRecord(blogEpisode,
////                            episodeRecord);
////                } else if (DBSortKey.ACT_STATEMENT.equals(sortKey)) {
////                    // Parse all act statements
////
////                    // Extract values from sort key
////                    Matcher actStatementMatcher = DBSortKey.ACT_STATEMENT.matcher(episodeRecord.getSort());
////                    if (!actStatementMatcher.matches() || actStatementMatcher.groupCount() != 3) {
////                        continue;
////                    }
////
////                    int actNumber = Integer.parseInt(actStatementMatcher.group(1));
////                    BlogEpisodeStatement blogEpisodeStatement = gson.fromJson(episodeRecord.getValue(),
////                            BlogEpisodeStatement.class);
////                    blogEpisode.getStatementList()
////                            .add(blogEpisodeStatement);
////
////
////                    // Build up a Set of all contributors as we go
////                    pipelineConfig.getContributors()
////                            .add(blogEpisodeStatement.getSpeakerName());
////
////                    // Configure Contributor at the episode level
////                    Map<String, BlogEpisodeContributor> episodeContributorMap = blogEpisode.getContributorMap();
////                    BlogEpisodeContributor blogEpisodeContributor =
////                            episodeContributorMap.computeIfAbsent(blogEpisodeStatement.getSpeakerName(),
////                                    (speakerName) -> new BlogEpisodeContributor(speakerName));
////                    blogEpisodeContributor.getStatements()
////                            .add(blogEpisodeStatement.getText());
////                    blogEpisodeContributor.getSpokenWords()
////                            .addAll(List.of(blogEpisodeStatement.getText()
////                                    .split("\\W+")));
////                    blogEpisodeContributor.getEpisodes()
////                            .computeIfAbsent(episodeNumber,
////                                    (epNum) -> blogEpisode.getEpisodeTitle());
////
////                    // Define contributor at the act level
////                    Map<Integer, BlogEpisodeAct> actMap = blogEpisode.getActMap();
////                    BlogEpisodeAct blogEpisodeAct = actMap.computeIfAbsent(actNumber,
////                            (num) -> new BlogEpisodeAct(num));
////                    blogEpisodeAct.getContributorMap()
////                            .computeIfAbsent(blogEpisodeContributor.getName(),
////                                    (name) -> blogEpisodeContributor);
////                    blogEpisodeAct.getStatementList()
////                            .add(blogEpisodeStatement);
////                }
////            }

            // Write this newly parsed episode to its own new location
            Files.write(outputPath,
                    gson.toJson(blogEpisode)
                            .getBytes(StandardCharsets.UTF_8));


            episodeSummaryList.add(blogEpisode.summarize());
        }

        // Read all lines of current episode list and remove last entry
        Path episodeListPath = pipelineConfig.getWorkingDirectory()
                .resolve(pipelineConfig.getEpisodeListFilepath());
        Files.write(episodeListPath,
                gson.toJson(episodeSummaryList)
                        .getBytes(StandardCharsets.UTF_8));
    }
//
//    private int getLatestEpNumber() {
//        // Figure out what the last episode we parsed was
//        PodcastDBDBRecord record = dbUtil.getItem(
//                Key.builder()
//                        .partitionValue(DBPartitionKey.PODCAST_NAME.getValue())
//                        .sortValue(DBSortKey.LATEST_EPISODE.getValue())
//                        .build());
//
//        // Get the number from the latest episode record
//        int latestEpNumber = Optional.ofNullable(record)
//                .map(PodcastDBDBRecord::getValue)
//                .map(Integer::parseInt)
//                .orElseGet(() -> 1);
//        return latestEpNumber;
//    }

//    /**
//     * Builds a List of ParsedEpisode objects based on raw files available in the local file system.
//     *
//     * @param allFilePaths List of Paths of every locally downloaded file
//     * @return List
//     */
//    private List<ParsedEpisode> buildParsedEpisodeList(List<Path> allFilePaths) {
//        List<ParsedEpisode> parsedEpisodeList = allFilePaths.stream()
//                .filter(path -> !episodeIsAlreadyParsed(path))
//                .map(episodeParser)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .collect(Collectors.toList());
//        return parsedEpisodeList;
//    }

//
//    /**
//     * Determines if a parsed representation of this episode is already present on disk.
//     *
//     * @param path Path to check
//     * @return true if a parsed representation is already available locally
//     */
//    boolean episodeIsAlreadyParsed(Path path) {
//        logger.info("Running matcher against path {}",
//                path.toString());
//        // Get episode number from filepath
//        Matcher matcher = Pattern.compile("episode-(\\d+).html")
//                .matcher(path.getFileName()
//                        .toString());
//        if (matcher.matches()) {
//            String epNum = matcher.toMatchResult()
//                    .group(1);
//            // Need to see if this ep is already present in the DB
//
//            PodcastDBDBRecord record = dbUtil.getItem(Key.builder()
//                    .partitionValue("TAL")
//                    .sortValue(String.format("EP%s#NAME",
//                            epNum))
//                    .build());
//            return Objects.nonNull(record);
//
//        }
//        return false;
//    }
}
