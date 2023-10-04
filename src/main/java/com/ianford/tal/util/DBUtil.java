package com.ianford.tal.util;

import com.google.gson.Gson;
import com.ianford.podcasts.model.db.DBPartitionKey;
import com.ianford.podcasts.model.db.DBSortKey;
import com.ianford.podcasts.model.db.PodcastDBDBRecord;
import com.ianford.podcasts.model.jekyll.BlogEpisode;
import com.ianford.podcasts.model.jekyll.BlogEpisodeAct;
import com.ianford.podcasts.model.jekyll.BlogEpisodeContributor;
import com.ianford.podcasts.model.jekyll.BlogEpisodeStatement;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class DBUtil {

    private static final Logger logger = LogManager.getLogger();

    private final DynamoDbTable<PodcastDBDBRecord> table;
    private final Gson gson;

    /**
     * Used to interact with our table
     *
     * @param table
     * @param gson
     */
    public DBUtil(DynamoDbTable<PodcastDBDBRecord> table, Gson gson) {
        this.table = table;
        this.gson = gson;
    }

    /**
     * Gets the most recent episode we looked at
     *
     * @return
     */
    public Optional<Integer> getMostRecentlyParsedEpisode() {
        // Find most recent episode stored in the table
        PodcastDBDBRecord record = table.getItem(Key.builder()
                .partitionValue(DBPartitionKey.PODCAST_NAME.getValue())
                .sortValue(DBSortKey.LATEST_EPISODE.getValue())
                .build());

        // Extract episode number from most recent episode and increment by 1 for download
        return Optional.ofNullable(record)
                .map(PodcastDBDBRecord::getValue)
                .map(Integer::parseInt);
    }

    /**
     * Get the name of the requested episode.
     *
     * @param episodeNumber The number of the episode to get the name of
     * @return Optional
     */
    public Optional<String> getEpisodeTitle(int episodeNumber) {
        PodcastDBDBRecord titleRecord = table.getItem(Key.builder()
                .partitionValue(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber))
                .sortValue(DBSortKey.EPISODE_NAME.getValue())
                .build());
        return Optional.ofNullable(titleRecord)
                .map(PodcastDBDBRecord::getValue);
    }

    /**
     * Gets all the statements in an episode
     *
     * @param episodeNumber
     * @return
     */
    public Optional<List<BlogEpisodeStatement>> getStatementsForEpisode(int episodeNumber) {
        // Get every act statement for the current episode in our loop
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                        .partitionValue(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber))
                        .sortValue("ACT_")
                        .build()))
                .build();
        PageIterable<PodcastDBDBRecord> actQueryResults = table.query(queryEnhancedRequest);
        SdkIterable<PodcastDBDBRecord> actQueryResultsIterable = actQueryResults.items();
        return Optional.ofNullable(actQueryResultsIterable.stream()
                .map(PodcastDBDBRecord::getValue)
                .filter(DBSortKey.ACT_STATEMENT::matches)
                .map(value -> gson.fromJson(value,
                        BlogEpisodeStatement.class))
                .collect(Collectors.toList()));
    }

    /**
     * Gets the name of a specific act
     *
     * @param episodeNumber
     * @param actNumber
     * @return
     */
    public Optional<String> getActName(int episodeNumber, int actNumber) {
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                        .partitionValue(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber))
                        .sortValue(String.format("ACT_%s#NAME",
                                actNumber))
                        .build()))
                .build();
        PageIterable<PodcastDBDBRecord> actQueryResults = table.query(queryEnhancedRequest);
        SdkIterable<PodcastDBDBRecord> actQueryResultsIterable = actQueryResults.items();
        return actQueryResultsIterable.stream()
                .findFirst()
                .map(PodcastDBDBRecord::getValue);
    }

    /**
     * Saves an individual record to the DB
     *
     * @param record Record to save
     */
    public void saveRecord(PodcastDBDBRecord record) {
        table.putItem(record);
    }

    /**
     * Retrieves an episode from the DB if possible.
     *
     * @param episodeNumber
     * @return
     */
    public Optional<BlogEpisode> getEpisode(int episodeNumber) {
        BlogEpisode episode = new BlogEpisode(episodeNumber);
        getEpisodeTitle(episodeNumber)
                .ifPresent(episode::setEpisodeTitle);
        getActsForEpisode(episodeNumber)
                .ifPresent(episode::setActMap);

        // Add every statement contained within an act to this episode
        episode.getActMap()
                .values()
                .stream()
                .map(BlogEpisodeAct::getStatementList)
                .forEach(statements -> episode.getStatementList()
                        .addAll(statements));

        // Add every contributor contained within an act to this episode
        episode.getActMap()
                .values()
                .stream()
                .map(BlogEpisodeAct::getContributorMap)
                .flatMap(map -> map.values()
                        .stream())
                .forEach(contributor -> {
                    episode.getContributorMap()
                            .put(contributor.getName(),
                                    contributor);
                    contributor.getEpisodes()
                            .put(episodeNumber,
                                    episode.getEpisodeTitle());
                });


        return Optional.of(episode);
    }

    public Set<String> getAllContributorNames() {
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                        .partitionValue(DBPartitionKey.CONTRIBUTOR.getValue())
                        .sortValue("NAME")
                        .build()))
                .build();
        PageIterable<PodcastDBDBRecord> contributorQueryResults = table.query(queryEnhancedRequest);
        SdkIterable<PodcastDBDBRecord> contributorQueryResultsIterable = contributorQueryResults.items();
        List<PodcastDBDBRecord> allContributorRecords = contributorQueryResultsIterable.stream()
                .filter(record -> DBSortKey.CONTRIBUTOR_STATEMENT.matches(record.getSort()))
                .sorted((recordA, recordB) -> recordA.getSort()
                        .compareTo(recordB.getSort()))
                .collect(Collectors.toList());
        Set<String> resultList = new HashSet<>();
        for (PodcastDBDBRecord contributorRecord : allContributorRecords) {
            Matcher matcher = DBSortKey.CONTRIBUTOR_STATEMENT.matcher(contributorRecord.getSort());
            if (!matcher.matches() || matcher.groupCount() != 5) continue;
            Optional.ofNullable(matcher.group(1))
                    .filter(StringUtils::isNotBlank)
                    .filter(name -> !"null".equalsIgnoreCase(name))
                    .map(resultList::add);
        }
        return resultList;
    }

    public Optional<BlogEpisodeContributor> getContributor(String contributorName) {
        logger.info("Loading records for contributor: {}",
                contributorName);
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                        .partitionValue(DBPartitionKey.CONTRIBUTOR.format(contributorName))
                        .sortValue(String.format("NAME_%s",contributorName))
                        .build()))
                .build();

        PageIterable<PodcastDBDBRecord> contributorQueryResults = table.query(queryEnhancedRequest);
        SdkIterable<PodcastDBDBRecord> contributorQueryResultsIterable = contributorQueryResults.items();
        List<PodcastDBDBRecord> recordList = contributorQueryResultsIterable.stream()
                .filter(record -> DBSortKey.CONTRIBUTOR_STATEMENT.matches(record.getSort()))
                .collect(Collectors.toList());
        BlogEpisodeContributor contributor = new BlogEpisodeContributor(contributorName);
        for (PodcastDBDBRecord statementRecord : recordList) {
            Matcher matcher = DBSortKey.CONTRIBUTOR_STATEMENT.matcher(statementRecord.getSort());
            if (!matcher.matches() || matcher.groupCount() != 5) continue;

            int episodeNumber = Integer.parseInt(matcher.group(2));
            String episodeTitle = this.getEpisodeTitle(episodeNumber)
                    .orElseThrow();

            contributor.getEpisodes()
                    .computeIfAbsent(episodeNumber,
                            (num) -> episodeTitle);

            int actNumber = Integer.parseInt(matcher.group(3));
            int statementNumber = Integer.parseInt(matcher.group(4));
            String timeStamp = matcher.group(5);

            BlogEpisodeStatement statement = new BlogEpisodeStatement(contributorName,
                    statementRecord.getValue());
            contributor.getStatements()
                    .add(statement.getText());
            contributor.getSpokenWords()
                    .addAll(Arrays.stream(statement.getText()
                                    .split("\\W"))
                            .collect(Collectors.toList()));
        }
        return Optional.of(contributor);
    }

    /**
     * Gets the act from the episode with the specified numbers.
     *
     * @param episodeNumber
     * @param actNumber
     * @return
     */
    public Optional<BlogEpisodeAct> getAct(int episodeNumber, int actNumber) {
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                        .partitionValue(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber))
                        .sortValue(String.format("ACT_%s#STATEMENT",
                                actNumber))
                        .build()))
                .build();
        PageIterable<PodcastDBDBRecord> actQueryResults = table.query(queryEnhancedRequest);
        SdkIterable<PodcastDBDBRecord> actQueryResultsIterable = actQueryResults.items();
        List<PodcastDBDBRecord> recordList = actQueryResultsIterable.stream()
                .filter(record -> DBSortKey.ACT_STATEMENT.matches(record.getSort()))
                .collect(Collectors.toList());


        BlogEpisodeAct act = new BlogEpisodeAct(actNumber);
        getActName(episodeNumber,
                actNumber)
                .ifPresent(act::setActName);


        for (PodcastDBDBRecord record : recordList) {
            Matcher matcher = DBSortKey.ACT_STATEMENT.matcher(record.getSort());

            // Pull this statement and add it to the act
            BlogEpisodeStatement actStatement = gson.fromJson(record.getValue(),
                    BlogEpisodeStatement.class);
            act.getStatementList()
                    .add(actStatement);

            // Create this contributor and add them to the act if they don't already exist
            BlogEpisodeContributor contributor = act.getContributorMap()
                    .computeIfAbsent(actStatement.getSpeakerName(),
                            name -> new BlogEpisodeContributor(name));
            contributor.getStatements()
                    .add(actStatement.getText());
            Set<String> statementSpokenWords = Arrays.stream(actStatement.getText()
                            .split("\\W"))
                    .collect(Collectors.toSet());
            contributor.getSpokenWords()
                    .addAll(statementSpokenWords);
        }
        return Optional.of(act);
    }

    /**
     * Retrieves all the acts for the given episode.
     *
     * @param episodeNumber
     * @return
     */
    public Optional<Map<Integer, BlogEpisodeAct>> getActsForEpisode(int episodeNumber) {
        // Get every act statement for the current episode in our loop
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                        .partitionValue(DBPartitionKey.EPISODE_NUMBER.format(episodeNumber))
                        .sortValue("ACT_")
                        .build()))
                .build();
        PageIterable<PodcastDBDBRecord> actQueryResults = table.query(queryEnhancedRequest);
        SdkIterable<PodcastDBDBRecord> actQueryResultsIterable = actQueryResults.items();
        List<PodcastDBDBRecord> recordList = actQueryResultsIterable.stream()
                .filter(record -> DBSortKey.ACT_NAME.matches(record.getSort()))
                .collect(Collectors.toList());

        Map<Integer, BlogEpisodeAct> actMap = new HashMap<>();
        for (PodcastDBDBRecord record : recordList) {
            Matcher matcher = DBSortKey.ACT_NAME.matcher(record.getSort());
            if (!matcher.matches() || matcher.groupCount() != 1)
                continue;
            int actNumber = Integer.parseInt(matcher.group(1));
            getAct(episodeNumber,
                    actNumber)
                    .ifPresent(act -> actMap.put(actNumber,
                            act));

        }
        return Optional.of(actMap);
    }
}
