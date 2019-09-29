package com.ianford.tal.guice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.ianford.podcasts.model.git.GitConfiguration;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class EnvironmentModule extends PrivateModule {

    public static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
    public static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
    public static final String DYNAMO_ENDPOINT = "DYNAMO_ENDPOINT";
    public static final String AWS_REGION = "AWS_REGION";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String GIT_REPO_URL = "GIT_REPO_URL";
    public static final String GIT_REMOTE = "GIT_REMOTE";
    public static final String GIT_BRANCH = "GIT_BRANCH";
    public static final String GIT_USERNAME = "GIT_USERNAME";
    public static final String GIT_PASSWORD = "GIT_PASSWORD";
    public static final String DOWNLOAD_LOCAL_PATH = "TAL_LOCAL_DOWNLOAD_DIR";
    public static final String EPISODE_LOCAL_PATH = "TAL_LOCAL_EPISODE_DIR";
    public static final String CONTRIBUTOR_LOCAL_PATH = "TAL_LOCAL_CONTRIBUTORS_DIR";
    public static final String JEKYLL_EPISODE_LIST_FILEPATH = "JEKYLL_EPISODE_LIST_FILEPATH";
    public static final String JEKYLL_CONTRIBUTOR_LIST_FILEPATH = "JEKYLL_CONTRIBUTOR_LIST_FILEPATH";
    public static final String POSTS_LOCAL_PATH = "TAL_LOCAL_POSTS_DIR";
    private static final Logger logger = LogManager.getLogger();
    private Function<String, String> envLoader;

    @Override
    protected void configure() {
        logger.info("Configuring EnvironmentModule");

        Dotenv dotenv = Dotenv.configure()
                .load();
        envLoader = (key) -> Optional.ofNullable(System.getenv(key))
                .orElse(dotenv.get(key));
    }

    @Provides
    @Exposed
    @Singleton
    Gson provideGson() {
        return new GsonBuilder().setPrettyPrinting()
                .create();
    }

    @Provides
    @Exposed
    @Singleton
    @Named(DYNAMO_ENDPOINT)
    String provideEndpoint() {
        return envLoader.apply(DYNAMO_ENDPOINT);
    }

    @Provides
    @Exposed
    @Singleton
    @Named(AWS_REGION)
    String provideAWSRegion() {
        return envLoader.apply(AWS_REGION);
    }

    @Provides
    @Exposed
    @Singleton
    @Named(TABLE_NAME)
    String provideTableName() {
        return envLoader.apply(TABLE_NAME);
    }

    @Provides
    @Exposed
    @Singleton
    @Named(DOWNLOAD_LOCAL_PATH)
    Path provideDownloadPath() {
        return Path.of(envLoader.apply(DOWNLOAD_LOCAL_PATH));
    }

    @Provides
    @Exposed
    @Singleton
    @Named(EPISODE_LOCAL_PATH)
    Path provideParsedEpisodePath() {
        return Path.of(envLoader.apply(EPISODE_LOCAL_PATH));
    }

    @Provides
    @Exposed
    @Singleton
    @Named(CONTRIBUTOR_LOCAL_PATH)
    Path providceContributorPath() {
        return Path.of(envLoader.apply(CONTRIBUTOR_LOCAL_PATH));
    }

    @Provides
    @Exposed
    @Singleton
    @Named(POSTS_LOCAL_PATH)
    Path providcePostsPath() {
        return Path.of(envLoader.apply(POSTS_LOCAL_PATH));
    }

    @Provides
    @Exposed
    @Singleton
    @Named(JEKYLL_EPISODE_LIST_FILEPATH)
    Path provideEpisodeListFilepath() {
        return Path.of(envLoader.apply(JEKYLL_EPISODE_LIST_FILEPATH));
    }

    @Provides
    @Exposed
    @Singleton
    @Named(JEKYLL_CONTRIBUTOR_LIST_FILEPATH)
    Path provideContributorListFilepath() {
        return Path.of(envLoader.apply(JEKYLL_CONTRIBUTOR_LIST_FILEPATH));
    }

    @Provides
    @Exposed
    @Singleton
    GitConfiguration provideGitConfiguration() {
        return new GitConfiguration(envLoader.apply(GIT_USERNAME),
                envLoader.apply(GIT_PASSWORD),
                envLoader.apply(GIT_REMOTE),
                envLoader.apply(GIT_BRANCH),
                envLoader.apply(GIT_REPO_URL));
    }

    @Provides
    @Exposed
    @Singleton
    @Named(AWS_ACCESS_KEY_ID)
    String provideAWSAccessKey() {
        return envLoader.apply("AWS_ACCESS_KEY_ID");
    }

    @Provides
    @Exposed
    @Singleton
    @Named(AWS_SECRET_ACCESS_KEY)
    String provideAWSSecret() {
        return envLoader.apply("AWS_SECRET_ACCESS_KEY");
    }
}
