package com.ianford.tal.steps;

import com.google.gson.Gson;
import com.ianford.podcasts.model.db.DBSortKey;
import com.ianford.podcasts.model.db.PodcastDBDBRecord;
import com.ianford.podcasts.model.jekyll.BlogEpisode;
import com.ianford.podcasts.model.jekyll.BlogEpisodeAct;
import com.ianford.tal.io.RawEpisodeParser;
import com.ianford.tal.model.PipelineConfig;
import com.ianford.tal.util.DBUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

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
        logger.info("Extracting act name from record");
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
        logger.info("Running BuildEpisodeDataStep");

        List<BlogEpisode.Summary> episodeSummaryList = new ArrayList<>();

        // Get every episode record up to the latest number
        int latestEpNumber = dbUtil.getMostRecentlyParsedEpisode()
                .orElseThrow();

        for (int episodeNumber = 1; episodeNumber <= latestEpNumber; episodeNumber++) {
            logger.info("Building data for episode {}",
                    episodeNumber);

            // Generate an output path for this episode and make sure it exists
            Path outputPath = pipelineConfig.buildPathForEpisode(episodeNumber);

            int finalEpisodeNumber = episodeNumber;
            boolean episodeIsTarget = pipelineConfig.getOptionalTargetEpisode()
                    .map(targetEp -> targetEp == finalEpisodeNumber)
                    .orElseGet(Boolean.FALSE::booleanValue);

            // If this episode already exists, and it's not the episode we're trying to rebuild then just load it
            if (!episodeIsTarget && outputPath.toFile()
                    .exists()) {
                logger.info("Path for this file already exists at {}",
                        outputPath);
                episodeSummaryList.add(gson.fromJson(Files.readString(outputPath),
                                BlogEpisode.class)
                        .summarize());
                continue;
            }

            // Create a new blogEpisode instance for this episode and set the title
            BlogEpisode blogEpisode = dbUtil.getEpisode(episodeNumber)
                    .orElseThrow();

            // Write this newly parsed episode to its own new location
            logger.info("Writing episode data to path {}",
                    outputPath);
            Files.write(outputPath,
                    gson.toJson(blogEpisode)
                            .getBytes(StandardCharsets.UTF_8));
            episodeSummaryList.add(blogEpisode.summarize());
        }

        // Read all lines of current episode list and remove last entry
        Path episodeListPath = pipelineConfig.getWorkingDirectory()
                .resolve(pipelineConfig.getEpisodeListFilepath());

        logger.info("Writing collected episode list to {}",
                episodeListPath);

        Files.write(episodeListPath,
                gson.toJson(episodeSummaryList)
                        .getBytes(StandardCharsets.UTF_8));
    }
}
