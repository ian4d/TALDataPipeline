package com.ianford.tal.steps;

import com.google.gson.Gson;
import com.ianford.podcasts.model.BasicPodcastRecord;
import com.ianford.podcasts.model.DBPartitionKey;
import com.ianford.podcasts.model.DBSortKey;
import com.ianford.podcasts.model.jekyll.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This step takes the most recent episode and updates our contributor model
 */
public class BackfillContributorDataStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final DynamoDbTable<BasicPodcastRecord> table;
    private final Gson gson;


    public BackfillContributorDataStep(DynamoDbTable<BasicPodcastRecord> table, Gson gson) {
        this.table = table;
        this.gson = gson;
    }

    @Override
    public void run() {
        logger.info("Running BuildContributorDataStep");
        // Grab all entries from that episode
        // Build local contributor model for all of those

        // Retrieve latest episode
        BasicPodcastRecord record = table.getItem(
                Key.builder()
                        .partitionValue(DBPartitionKey.PODCAST_NAME.getValue())
                        .sortValue(DBSortKey.LATEST_EPISODE.getValue())
                        .build());

        // Get the number from the latest episode record
        int latestEpNumber = Optional.ofNullable(record)
                .map(BasicPodcastRecord::getValue)
                .map(Integer::parseInt)
                .orElseGet(() -> 1);


        // Build a query that returns all entries from the table for this episode
        // "begins_with('STATEMENT')"
        Key statementKey = Key.builder()
                .partitionValue(DBPartitionKey.EPISODE_NUMBER.format(latestEpNumber))
                .sortValue("STATEMENT")
                .build();
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(statementKey);
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        Map<String, List<String>> statementsByContributor = new HashMap<>();


        PageIterable<BasicPodcastRecord> queryResults = table.query(queryEnhancedRequest);
        SdkIterable<BasicPodcastRecord> iterable = queryResults.items();
        for (BasicPodcastRecord queryRecord : iterable) {
            logger.info("STATEMENT: {}",
                        queryRecord);
            // Deserialize statement
            Statement statement = gson.fromJson(queryRecord.getValue(),
                                                Statement.class);
            List<String> statementList = statementsByContributor.computeIfAbsent(statement.getSpeakerName(),
                                                                                 (str) -> new ArrayList<>());
            statementList.add(statement.getText());

        }

        logger.info("statementsByContributor(): {}",
                    statementsByContributor);

        // Load existing set of statements for this contributor


        /**
         * [{
         *   "name": "Ira Glass",
         *   "url": "ira-glass",
         *   "job": "HOST",
         *   "age": "60",
         *   "episodes": [
         *     "ep1",
         *     "ep2",
         *     "ep3"
         *   ]
         * },{
         *     "name": "Ian Ford",
         *     "url": "ian-ford",
         *     "job": "megafan",
         *     "age": "37",
         *     "episodes": [
         *         "ep1",
         *         "ep2",
         *         "ep3"
         *       ]
         * }]
         */

    }
}
