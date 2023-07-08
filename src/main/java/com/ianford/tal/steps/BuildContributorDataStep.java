package com.ianford.tal.steps;

import com.google.gson.Gson;
import com.ianford.podcasts.model.db.DBPartitionKey;
import com.ianford.podcasts.model.db.DBSortKey;
import com.ianford.podcasts.model.db.PodcastDBDBRecord;
import com.ianford.podcasts.model.jekyll.BlogEpisodeContributor;
import com.ianford.tal.model.PipelineConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * This step takes the most recent episode and updates our contributor model
 */
public class BuildContributorDataStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();
    private final DynamoDbTable<PodcastDBDBRecord> table;
    private final Gson gson;


    public BuildContributorDataStep(DynamoDbTable<PodcastDBDBRecord> table, Gson gson) {
        this.table = table;
        this.gson = gson;
    }

    @Override
    public void run(PipelineConfig pipelineConfig) throws IOException {
        logger.info("Running BuildContributorDataStep");


        Map<String, BlogEpisodeContributor> contributorMap = new HashMap<>();
        for (String contributorName : pipelineConfig.getContributors()) {

            if (StringUtils.isBlank(contributorName)) {
                continue;
            }

            BlogEpisodeContributor blogEpisodeContributor = contributorMap.computeIfAbsent(contributorName,
                    (name) -> new BlogEpisodeContributor(contributorName));

            QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                            .partitionValue(DBPartitionKey.CONTRIBUTOR.format(contributorName))
                            .sortValue("EP_")
                            .build()))
                    .build();

            PageIterable<PodcastDBDBRecord> contributorQueryResults = table.query(queryEnhancedRequest);
            SdkIterable<PodcastDBDBRecord> contributorQueryResultsIterable = contributorQueryResults.items();
            for (PodcastDBDBRecord episodeRecord : contributorQueryResultsIterable) {

                // We're only looking for act statements right now...right?
                DBSortKey sortKey = DBSortKey.resolveKeyType(episodeRecord.getSort())
                        .orElseThrow(() -> new RuntimeException("Could not match this sort key"));

                if (DBSortKey.CONTRIBUTOR_STATEMENT.equals(sortKey)) {
                    Matcher contributorStatementMatcher =
                            DBSortKey.CONTRIBUTOR_STATEMENT.matcher(episodeRecord.getSort());
                    if (!contributorStatementMatcher.matches() || contributorStatementMatcher.groupCount() != 4) {
                        logger.info("Mismatched key: {}",
                                episodeRecord.getSort());
                        continue;
                    }

                    int episodeNumber = Integer.parseInt(contributorStatementMatcher.group(1));
                    int actNumber = Integer.parseInt(contributorStatementMatcher.group(2));
                    int statementNumber = Integer.parseInt(contributorStatementMatcher.group(3));
                    String timestamp = contributorStatementMatcher.group(4);

                    // Make sure episode title is represented
                    String episodeName = table.getItem(Key.builder()
                                    .partitionValue(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber))
                                    .sortValue(DBSortKey.EPISODE_NAME.name())
                                    .build())
                            .getValue();
                    blogEpisodeContributor.getEpisodes()
                            .computeIfAbsent(episodeNumber,
                                    (num) -> episodeName);

                    // Add newest statement
                    blogEpisodeContributor.getStatements()
                            .add(episodeRecord.getValue());

                    // Add newest spoken words
                    blogEpisodeContributor.getSpokenWords()
                            .addAll(Arrays.asList(episodeRecord.getValue()
                                    .split("\\W+")));

                }

            }

            // Write data file for this contributor

            Path outputPath = pipelineConfig.getWorkingDirectory()
                    .resolve(pipelineConfig.getLocalContributorDirectory())
                    .resolve(String.format("%s.json",
                            contributorName));
            // Write this newly parsed episode to its own new location
            Files.write(outputPath,
                    gson.toJson(blogEpisodeContributor)
                            .getBytes(StandardCharsets.UTF_8));
        }

        Path contributorListPath = pipelineConfig.getWorkingDirectory()
                .resolve(pipelineConfig.getContributorListFilepath());
        Files.write(contributorListPath,
                gson.toJson(contributorMap.values())
                        .getBytes(StandardCharsets.UTF_8));
    }


    private int getLatestEpNumber() {
        // Figure out what the last episode we parsed was
        PodcastDBDBRecord record = table.getItem(
                Key.builder()
                        .partitionValue(DBPartitionKey.PODCAST_NAME.getValue())
                        .sortValue(DBSortKey.LATEST_EPISODE.getValue())
                        .build());

        // Get the number from the latest episode record
        int latestEpNumber = Optional.ofNullable(record)
                .map(PodcastDBDBRecord::getValue)
                .map(Integer::parseInt)
                .orElseGet(() -> 1);
        return latestEpNumber;
    }
}
