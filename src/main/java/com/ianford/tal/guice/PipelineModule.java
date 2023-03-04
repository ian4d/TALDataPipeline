package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.ianford.podcasts.model.BasicEpisodeRecord;
import com.ianford.podcasts.tal.io.RawEpisodeParser;
import com.ianford.podcasts.tal.util.EpisodeDownloader;
import com.ianford.tal.Pipeline;
import com.ianford.tal.steps.BackfillDatabaseStep;
import com.ianford.tal.steps.DownloadEpisodeStep;
import com.ianford.tal.steps.PipelineStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to provide and configure the pipeline that runs all of our pipeline steps
 */
@SuppressWarnings("unused")
public class PipelineModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring PipelineModule");
    }

    /**
     * Provides the object that will be invoked to run our steps
     *
     * @param pipelineStepList A List of steps to execute
     * @return Pipeline
     */
    @Provides
    @Exposed
    Pipeline providePipeline(List<PipelineStep> pipelineStepList) {
        return new Pipeline(pipelineStepList);
    }


    /**
     * Provides a step that is used to download new episodes locally
     *
     * @param episodeDownloader Downloads the latest episode of the show
     * @return DownloadEpisodeStep
     */
    @Provides
    @Exposed
    DownloadEpisodeStep provideDownloadEpisodeStep(EpisodeDownloader episodeDownloader,
                                                   DynamoDbTable<BasicEpisodeRecord> table) {
        return new DownloadEpisodeStep(episodeDownloader, table);
    }

    /**
     * Provides a step that updates our DB based on locally downloaded episodes.
     *
     * @param table         Used to write to our DB.
     * @param episodeParser Used to parse HTML episodes.
     * @return BackfillDatabaseStep
     */
    @Provides
    @Exposed
    BackfillDatabaseStep provideBackfillStep(DynamoDbTable<BasicEpisodeRecord> table,
                                             RawEpisodeParser episodeParser) {
        return new BackfillDatabaseStep(table, episodeParser);
    }

    /**
     * Provides steps that will be executed as part of the pipeline
     */
    @Provides
    List<PipelineStep> providePipelineSteps(DownloadEpisodeStep downloadEpisodeStep,
                                            BackfillDatabaseStep backfillDatabaseStep
    ) {
        List<PipelineStep> steps = new ArrayList<>();

        // Download any new/missing episodes
        steps.add(downloadEpisodeStep);
        steps.add(backfillDatabaseStep);


        return steps;
    }
}
