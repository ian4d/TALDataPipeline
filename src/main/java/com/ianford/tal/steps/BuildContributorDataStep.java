package com.ianford.tal.steps;

import com.google.gson.Gson;
import com.ianford.podcasts.model.jekyll.BlogEpisodeContributor;
import com.ianford.tal.model.PipelineConfig;
import com.ianford.tal.util.DBUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This step takes the most recent episode and updates our contributor model
 */
public class BuildContributorDataStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final Set<String> excludedContributors;

    private final DBUtil dbUtil;
    private final Gson gson;

    public BuildContributorDataStep(DBUtil dbUtil, Gson gson, Set<String> excludedContributors) {
        this.dbUtil = dbUtil;
        this.gson = gson;
        this.excludedContributors = excludedContributors;
    }

    @Override
    public void run(PipelineConfig pipelineConfig) throws IOException {
        logger.info("Running BuildContributorDataStep");

        List<BlogEpisodeContributor> allContributors = new ArrayList<>();
        Set<String> allContributorNames = dbUtil.getAllContributorNames();
        logger.info("Identified {} contributors in the table", allContributorNames.size());
        for (String contributorName : allContributorNames) {
            logger.info("Building data for contributor {}", contributorName);
            Optional<BlogEpisodeContributor> optionalContributor = dbUtil.getContributor(contributorName);
            if (!optionalContributor.isPresent()) continue;
            BlogEpisodeContributor contributor = optionalContributor.get();

            Path outputPath = pipelineConfig.getWorkingDirectory()
                    .resolve(pipelineConfig.getLocalContributorDirectory())
                    .resolve(String.format("%s.json",
                            contributor.getName()));

            logger.info("Writing data for contributor {} to path {}", contributorName, outputPath);

            // Write this newly parsed contributor to its own new location
            Files.write(outputPath,
                    gson.toJson(contributor)
                            .getBytes(StandardCharsets.UTF_8));
        }
//        }

        // Write actual contributor list to the page???
        Path contributorListPath = pipelineConfig.getWorkingDirectory()
                .resolve(pipelineConfig.getContributorListFilepath());

        logger.info("Writing collected contributor list info to {}", contributorListPath);
        String serializedContributorList = gson.toJson(allContributorNames
                .stream().map(name -> Collections.singletonMap("name", name))
                .collect(Collectors.toList()));
        Files.write(contributorListPath,
                serializedContributorList
                        .getBytes(StandardCharsets.UTF_8));

    }

}
