package com.ianford.tal.guice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.util.Optional;

@SuppressWarnings("unused")
public class EnvironmentModule extends PrivateModule {

    public static final String DYNAMO_ENDPOINT = "DYNAMO_ENDPOINT";
    public static final String AWS_REGION = "AWS_REGION";
    public static final String TABLE_NAME = "TABLE_NAME";
    private static final Logger logger = LogManager.getLogger();

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
        return Optional.ofNullable(System.getenv(DYNAMO_ENDPOINT))
                .orElseGet(() -> "http://localhost:8000");
    }

    @Provides
    @Exposed
    @Named(AWS_REGION)
    String provideAWSRegion() {
        return Optional.ofNullable(System.getenv(AWS_REGION))
                .orElseGet(() -> "us-east-1");
    }

    @Provides
    @Exposed
    @Named(TABLE_NAME)
    String provideTableName() {
        return Optional.ofNullable(System.getenv(TABLE_NAME))
                .orElseGet(() -> "EpisodeRecords");
    }
}
