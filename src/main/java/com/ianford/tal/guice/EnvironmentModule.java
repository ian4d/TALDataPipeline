package com.ianford.tal.guice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.ianford.podcasts.model.git.GitConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class EnvironmentModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();
    public static final String DYNAMO_ENDPOINT = "DYNAMO_ENDPOINT";
    public static final String AWS_REGION = "AWS_REGION";
    public static final String TABLE_NAME = "TABLE_NAME";

    public static final String GIT_REPO_URL = "GIT_REPO_URL";
    public static final String GIT_REMOTE = "GIT_REMOTE";
    public static final String GIT_USERNAME = "GIT_USERNAME";
    public static final String GIT_PASSWORD = "GIT_PASSWORD";
    public static final String DOWNLOAD_LOCAL_PATH = "TAL_LOCAL_DOWNLOAD_DIR";
    public static final String EPISODE_LOCAL_PATH = "TAL_LOCAL_EPISODE_DIR";
    public static final String CONTRIBUTOR_LOCAL_PATH = "TAL_LOCAL_CONTRIBUTORS_DIR";
    public static final String JEKYLL_EPISODE_LIST_FILEPATH = "JEKYLL_EPISODE_LIST_FILEPATH";
    public static final String JEKYLL_CONTRIBUTOR_LIST_FILEPATH = "JEKYLL_CONTRIBUTOR_LIST_FILEPATH";
    public static final String POSTS_LOCAL_PATH = "TAL_LOCAL_POSTS_DIR";

    @Override
    protected void configure() {
        logger.info("Configuring EnvironmentModule");
    }

    @Provides
    @Exposed
    Gson provideGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    @Provides
    @Exposed
    @Named(DYNAMO_ENDPOINT)
    String provideEndpoint() {
        return System.getenv(DYNAMO_ENDPOINT);
    }

    @Provides
    @Exposed
    @Named(AWS_REGION)
    String provideAWSRegion() {
        return System.getenv(AWS_REGION);
    }

    @Provides
    @Exposed
    @Named(TABLE_NAME)
    String provideTableName() {
        return System.getenv(TABLE_NAME);
    }

    @Provides
    @Exposed
    @Named(DOWNLOAD_LOCAL_PATH)
    Path provideDownloadPath() {
        return Path.of(System.getenv(DOWNLOAD_LOCAL_PATH));
    }

    @Provides
    @Exposed
    @Named(EPISODE_LOCAL_PATH)
    Path provideParsedEpisodePath() {
        return Path.of(System.getenv(EPISODE_LOCAL_PATH));
    }

    @Provides
    @Exposed
    @Named(CONTRIBUTOR_LOCAL_PATH)
    Path providceContributorPath() {
        return Path.of(System.getenv(CONTRIBUTOR_LOCAL_PATH));
    }

    @Provides
    @Exposed
    @Named(POSTS_LOCAL_PATH)
    Path providcePostsPath() {
        return Path.of(System.getenv(POSTS_LOCAL_PATH));
    }

    @Provides
    @Exposed
    @Named(JEKYLL_EPISODE_LIST_FILEPATH)
    Path provideEpisodeListFilepath() {
        return Path.of(System.getenv(JEKYLL_EPISODE_LIST_FILEPATH));
    }

    @Provides
    @Exposed
    @Named(JEKYLL_CONTRIBUTOR_LIST_FILEPATH)
    Path provideContributorListFilepath() {
        return Path.of(System.getenv(JEKYLL_CONTRIBUTOR_LIST_FILEPATH));
    }

    @Provides
    @Exposed
    GitConfiguration provideGitConfiguration() {
        return new GitConfiguration(System.getenv(GIT_USERNAME),
                System.getenv(GIT_PASSWORD),
                System.getenv(GIT_REMOTE),
                System.getenv(GIT_REPO_URL));
    }
}
