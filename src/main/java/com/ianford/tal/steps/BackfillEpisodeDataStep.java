package com.ianford.tal.steps;

import com.google.gson.Gson;
import com.ianford.podcasts.model.BasicPodcastRecord;
import com.ianford.podcasts.model.DBPartitionKey;
import com.ianford.podcasts.model.DBSortKey;
import com.ianford.podcasts.model.jekyll.Act;
import com.ianford.podcasts.model.jekyll.Contributor;
import com.ianford.podcasts.model.jekyll.Episode;
import com.ianford.podcasts.model.jekyll.Statement;
import com.ianford.podcasts.tal.io.RawEpisodeParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Takes all existing raw data, parses it, and writes it to DDB and to our serialized representation used for Jekyll.
 */
public class BackfillEpisodeDataStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final DynamoDbTable<BasicPodcastRecord> table;
    private final RawEpisodeParser episodeParser;

    private final Gson gson;

    /**
     * Constructor.
     *
     * @param table         Used to store records we are backfilling.
     * @param episodeParser Used to convert raw HTML files into episode data.
     * @param gson          Used to serialize and deserialize statements
     */
    @SuppressWarnings("unused")
    public BackfillEpisodeDataStep(DynamoDbTable<BasicPodcastRecord> table, RawEpisodeParser episodeParser, Gson gson) {
        this.table = table;
        this.episodeParser = episodeParser;
        this.gson = gson;
    }

    @SuppressWarnings("unused")
    @Override
    public void run() throws IOException {
        // Converted all files into data
        List<Path> allFilePaths =
                Files.list(Path.of("data/downloads/raw"))
                        .collect(Collectors.toList());

        // Write all the records in our raw file to our DB
        List<BasicPodcastRecord> writtenContents = allFilePaths.stream()
                .peek(path -> logger.info("Reading file at {}",
                                          path.toString()))
                .filter(path -> !episodeIsAlreadyParsed(path))
                .map(episodeParser)
                .flatMap(List::stream)
                .peek(record -> logger.debug(record.toString()))
                .map(this::writeRecordToDB)// write each record to DB
                .collect(Collectors.toList());

        // These are the records we actually wrote
        logger.debug("writtenContents: {}",
                     writtenContents.size());

        // Clean up raw data when done
        allFilePaths.stream()
                .forEach(path -> path.toFile()
                        .deleteOnExit());


        Map<Integer, Episode> episodeMap = new HashMap<>();
//        Map<String, Contributor> contributorMap = new HashMap<>();

        // Iterate over written records
        for (BasicPodcastRecord record : writtenContents) {
            String primaryKey = record.getPrimaryKey();
            Matcher episodeNumberMatcher = DBPartitionKey.EPISODE_NUMBER.matcher(primaryKey);
            if (!episodeNumberMatcher.matches()) continue;

            int episodeNumber = Integer.parseInt(episodeNumberMatcher.group(1));
            int actNumber;
            Act act;

            Episode episode = episodeMap.computeIfAbsent(episodeNumber,
                                                         (epNum) -> new Episode(epNum));

            Statement statement;

            String sortValue = record.getSort();
            DBSortKey keyType = DBSortKey.resolveKeyType(sortValue)
                    .orElseThrow();
            // What type of record is this?
            logger.info("KEY TYPE: {}",
                        keyType);
            switch (keyType) {
                case ACT_NAME:

                    // See if the episode contains this act or not

                    Matcher actNameMatcher = DBSortKey.ACT_NAME.matcher(sortValue);
                    if (!actNameMatcher.matches()) continue;
                    actNumber = Integer.parseInt(actNameMatcher.group(1));

                    act = episode.getActMap()
                            .computeIfAbsent(actNumber,
                                             (num) -> new Act(num));
                    act.setActName(record.getValue());

                    break;
                case ACT_STATEMENT:
                    Matcher actStatementMatcher = DBSortKey.ACT_STATEMENT.matcher(sortValue);
                    if (!actStatementMatcher.matches()) continue;

                    actNumber = Integer.parseInt(actStatementMatcher.group(1));
                    act = episode.getActMap()
                            .computeIfAbsent(actNumber,
                                             (num) -> new Act(num));
                    statement = gson.fromJson(record.getValue(),
                                              Statement.class);
                    act.getStatementList()
                            .add(statement);


                    Contributor contributor = episode.getContributorMap()
                            .computeIfAbsent(statement.getSpeakerName(),
                                             (name) -> new Contributor(name));
                    contributor.getEpisodes()
                            .computeIfAbsent(episodeNumber,
                                             (num) -> episode.getEpisodeTitle());
                    contributor.getStatements()
                            .add(statement.getText());
                    contributor.getSpokenWords()
                            .addAll(List.of(statement.getText()
                                                    .split("\\s")));

                    break;
                case EPISODE_NAME:
                    episode.setEpisodeTitle(record.getValue());
                    break;
                case EPISODE_STATEMENT:
                    statement = gson.fromJson(record.getValue(),
                                              Statement.class);
                    episode.getStatementList()
                            .add(statement);
                    break;
                case LATEST_EPISODE:
                    // we don't care about this I don't think
                    break;
            }


        }

        // Could write the entrySet instead of we want to avoid numeric keys
        for (Integer epNum : episodeMap.keySet()) {
            Path outputPath = Path.of(String.format("data/blog/episodes/%s.json",
                                                    epNum));
            String serializedEpisode = gson.toJson(episodeMap.get(epNum));
            Files.writeString(outputPath,
                              serializedEpisode);
        }

    }

    boolean episodeIsAlreadyParsed(Path path) {
        logger.info("Running matcher against path {}",
                    path.toString());
        // Get episode number from filepath
        Matcher matcher = Pattern.compile("episode-(\\d+).html")
                .matcher(path.getFileName()
                                 .toString());
        if (matcher.matches()) {
            String epNum = matcher.toMatchResult()
                    .group(1);
            // Need to see if this ep is already present in the DB

            BasicPodcastRecord record = table.getItem(Key.builder()
                                                              .partitionValue("TAL")
                                                              .sortValue(String.format("EP%s#NAME",
                                                                                       epNum))
                                                              .build());
            return Objects.nonNull(record);

        }
        return false;
    }

    BasicPodcastRecord writeRecordToDB(BasicPodcastRecord record) {
        logger.debug("Writing record: {}",
                     record.toString());
        table.putItem(record);
        return record;
    }
}
