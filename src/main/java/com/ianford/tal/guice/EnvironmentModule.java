package com.ianford.tal.guice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.ianford.podcasts.model.GitConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;

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

    @Override
    protected void configure() {
        logger.info("Configuring EnvironmentModule");
    }

    @Provides
    @Exposed
    Gson provideGson() {
        return new GsonBuilder().create();
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
    GitConfiguration provideGitConfiguration() {
        return new GitConfiguration(System.getenv(GIT_USERNAME),
                System.getenv(GIT_PASSWORD),
                System.getenv(GIT_REMOTE),
                System.getenv(GIT_REPO_URL));
    }
}
