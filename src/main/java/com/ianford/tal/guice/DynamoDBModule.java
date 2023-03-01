package com.ianford.tal.guice;

import com.google.inject.PrivateModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

public class DynamoDBModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring DynamoDBModule");
    }

    DynamoDbClient provideClient() {
        return DynamoDbClient.builder()
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("dummy-key", "dummy-secret")))
                .endpointOverride(URI.create("http://localhost:8000"))
                .region(Region.US_WEST_2)
                .build();

    }

    DynamoDBMapp


}
