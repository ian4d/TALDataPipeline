package com.ianford.tal.guice;

import com.google.gson.Gson;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.ianford.podcasts.model.git.GitConfiguration;
import com.ianford.tal.Pipeline;
import com.ianford.tal.io.RawEpisodeParser;
import com.ianford.tal.model.PipelineConfig;
import com.ianford.tal.steps.BuildContributorDataStep;
import com.ianford.tal.steps.BuildEpisodeDataStep;
import com.ianford.tal.steps.CreateBlogPostStep;
import com.ianford.tal.steps.DownloadEpisodeStep;
import com.ianford.tal.steps.GithubCommitStep;
import com.ianford.tal.steps.ParseEpisodeStep;
import com.ianford.tal.steps.PipelineStep;
import com.ianford.tal.steps.PrepareLocalRepoStep;
import com.ianford.tal.util.DBUtil;
import com.ianford.tal.util.EpisodeDownloader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    @Singleton
    Pipeline providePipeline(List<PipelineStep> pipelineStepList) {
        return new Pipeline(pipelineStepList);
    }

    /**
     * Provides initial configuration data for pipeline
     *
     * @return PipelineConfig
     */
    @Provides
    @Exposed
    @Singleton
    PipelineConfig providePipelineConfig(
            @Named(EnvironmentModule.DOWNLOAD_LOCAL_PATH) Path localDownloadPath,
            @Named(EnvironmentModule.EPISODE_LOCAL_PATH) Path localParsedEpisodePath,
            @Named(EnvironmentModule.CONTRIBUTOR_LOCAL_PATH) Path localContributorPath,
            @Named(EnvironmentModule.POSTS_LOCAL_PATH) Path localPostsPath,
            @Named(EnvironmentModule.JEKYLL_EPISODE_LIST_FILEPATH) Path episodeListFilepath,
            @Named(EnvironmentModule.JEKYLL_CONTRIBUTOR_LIST_FILEPATH) Path contributorListFilepath) {
        PipelineConfig pipelineConfig = new PipelineConfig();
        pipelineConfig.setLocalDownloadDirectory(localDownloadPath);
        pipelineConfig.setLocalParsedEpisodeDirectory(localParsedEpisodePath);
        pipelineConfig.setLocalContributorDirectory(localContributorPath);
        pipelineConfig.setLocalPostsDirectory(localPostsPath);
        pipelineConfig.setContributorListFilepath(contributorListFilepath);
        pipelineConfig.setEpisodeListFilepath(episodeListFilepath);
        return pipelineConfig;
    }


    /**
     * Provides a step that configures a local repo for our pipeline
     *
     * @param gitConfiguration Configuration needed to interact with the git repo that holds our website.
     * @return PrepareLocalRepoStep
     */
    @Provides
    @Singleton
    PrepareLocalRepoStep providePrepareLocalRepoStep(GitConfiguration gitConfiguration) {
        return new PrepareLocalRepoStep(gitConfiguration);
    }

    /**
     * Provides a step that is used to download new episodes locally
     *
     * @param episodeDownloader Downloads the latest episode of the show
     * @return DownloadEpisodeStep
     */
    @Provides
    @Singleton
    DownloadEpisodeStep provideDownloadEpisodeStep(
            EpisodeDownloader episodeDownloader,
            DBUtil dbUtil) {
        return new DownloadEpisodeStep(episodeDownloader,
                dbUtil);
    }

    /**
     * Provides a step that updates our DB based on locally downloaded episodes.
     *
     * @param dbUtil        Used to write to our DB.
     * @param episodeParser Used to parse HTML episodes.
     * @return ParseEpisodeStep
     */
    @Provides
    @Singleton
    ParseEpisodeStep provideParseEpisodeStep(DBUtil dbUtil,
            RawEpisodeParser episodeParser) {
        return new ParseEpisodeStep(dbUtil,
                episodeParser);
    }

    /**
     * Provides a step that updates our DB based on locally downloaded episodes.
     *
     * @param dbUtil        Used to write to our DB.
     * @param episodeParser Used to parse HTML episodes.
     * @param gson          Used to handle serialization of jekyll data.
     * @return BackfillDatabaseStep
     */
    @Provides
    @Singleton
    BuildEpisodeDataStep provideBuildEpisodeDataStep(DBUtil dbUtil,
            RawEpisodeParser episodeParser, Gson gson) {
        return new BuildEpisodeDataStep(dbUtil,
                episodeParser,
                gson);
    }

    /**
     * Provides a step that updates our DB based on new contributor data.
     *
     * @param dbUtil Used to write to our DB.
     * @param gson Used for serialization.
     * @param excludedContributors Contributors who
     * @return BackfillContributorDataStep
     */
    @Provides
    @Singleton
    BuildContributorDataStep provideBuildContributorDataStep(DBUtil dbUtil,
            Gson gson,
            @Named(DataFilterModule.EXCLUDED_CONTRIBUTOR_SET) Set<String> excludedContributors) {
        return new BuildContributorDataStep(dbUtil,
                gson,
                excludedContributors);
    }

    /**
     * Provides a step that creates a new blog post based on recent updates.
     *
     * @return CreateBlogPostStep
     */
    @Provides
    @Singleton
    CreateBlogPostStep provideCreateBlogPostStep() {
        return new CreateBlogPostStep();
    }

    /**
     * Provides a step that commits changes to our jekyll blog.
     *
     * @param gitConfiguration Object used when interacting with Git.
     * @return GithubCommitStep
     */
    @Provides
    @Singleton
    GithubCommitStep provideGithubCommitStep(GitConfiguration gitConfiguration) {
        return new GithubCommitStep(gitConfiguration);
    }

    /**
     * Provides steps that will be executed as part of the pipeline
     */
    @Provides
    List<PipelineStep> providePipelineSteps(
            PrepareLocalRepoStep prepareLocalRepoStep,
            DownloadEpisodeStep downloadEpisodeStep,
            ParseEpisodeStep parseEpisodeStep,
            BuildEpisodeDataStep buildEpisodeDataStep,
            BuildContributorDataStep buildContributorDataStep,
            CreateBlogPostStep createBlogPostStep,
            GithubCommitStep githubCommitStep
                                           ) {
        List<PipelineStep> steps = new ArrayList<>();

        // Clone the website repo locally so we can make edits to it
        steps.add(prepareLocalRepoStep);

        // Download the latest episode
        steps.add(downloadEpisodeStep);

        // Parse new episodes and write them to the DB
        steps.add(parseEpisodeStep);

        // Parse the data from the latest episode
        steps.add(buildEpisodeDataStep);

        // Update Contributor Data
        steps.add(buildContributorDataStep);

        // Create a blog post describing the recent changes
        steps.add(createBlogPostStep);

        // Commit all the changes back to the github repository
        steps.add(githubCommitStep);

        return steps;
    }
}
