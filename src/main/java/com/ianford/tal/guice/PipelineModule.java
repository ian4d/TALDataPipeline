package com.ianford.tal.guice;

import com.google.gson.Gson;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.ianford.podcasts.model.BasicPodcastRecord;
import com.ianford.podcasts.model.GitConfiguration;
import com.ianford.tal.io.RawEpisodeParser;
import com.ianford.tal.util.EpisodeDownloader;
import com.ianford.tal.Pipeline;
import com.ianford.tal.steps.BackfillContributorDataStep;
import com.ianford.tal.steps.BackfillEpisodeDataStep;
import com.ianford.tal.steps.DownloadEpisodeStep;
import com.ianford.tal.steps.GithubCommitStep;
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
    DownloadEpisodeStep provideDownloadEpisodeStep(EpisodeDownloader episodeDownloader,
            DynamoDbTable<BasicPodcastRecord> table) {
        return new DownloadEpisodeStep(episodeDownloader,
                table);
    }

    /**
     * Provides a step that updates our DB based on locally downloaded episodes.
     *
     * @param table         Used to write to our DB.
     * @param episodeParser Used to parse HTML episodes.
     * @param gson          Used to handle serialization of jekyll data.
     * @return BackfillDatabaseStep
     */
    @Provides
    BackfillEpisodeDataStep provideBackfillEpisodeDataStep(DynamoDbTable<BasicPodcastRecord> table,
            RawEpisodeParser episodeParser, Gson gson) {
        return new BackfillEpisodeDataStep(table,
                episodeParser,
                gson);
    }

    /**
     * Provides a step that updates our DB based on new contributor data.
     *
     * @param table Used to write to our DB.
     * @return BackfillContributorDataStep
     */
    @Provides
    BackfillContributorDataStep provideBackfillContributorDataStep(DynamoDbTable<BasicPodcastRecord> table,
            Gson gson) {
        return new BackfillContributorDataStep(table,
                gson);
    }

    /**
     * Provides a step that commits changes to our jekyll blog.
     *
     * @param gitConfiguration Object used when interacting with Git.
     * @return GithubCommitStep
     */
    @Provides
    GithubCommitStep provideGithubCommitStep(GitConfiguration gitConfiguration) {
        return new GithubCommitStep(gitConfiguration,
                fileSaver);
    }

    /**
     * Provides steps that will be executed as part of the pipeline
     */
    @Provides
    List<PipelineStep> providePipelineSteps(DownloadEpisodeStep downloadEpisodeStep,
            BackfillEpisodeDataStep backfillEpisodeDataStep,
            BackfillContributorDataStep backfillContributorDataStep,
            GithubCommitStep githubCommitStep
                                           ) {
        List<PipelineStep> steps = new ArrayList<>();

        // Download any new/missing episodes
        steps.add(downloadEpisodeStep);
        steps.add(backfillEpisodeDataStep);
//        steps.add(backfillContributorDataStep);
        steps.add(githubCommitStep);

        return steps;
    }
}
