package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.ianford.podcasts.model.BasicEpisodeRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.net.URI;

@SuppressWarnings("unused")
public class DynamoDBModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring DynamoDBModule");
    }

    /**
     * Provides a basic DynamoDbClient.
     *
     * @param dynamoEndpoint Endpoint to connect to.
     * @param region         Region to make requests in.
     * @return DynamoDbClient
     */
    @Provides
    DynamoDbClient provideClient(
            @Named(EnvironmentModule.DYNAMO_ENDPOINT) String dynamoEndpoint,
            @Named(EnvironmentModule.AWS_REGION) String region) {
        logger.info("Connecting to DDB Local at Endpoint: {}", dynamoEndpoint);
        return DynamoDbClient.builder()
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("dummy-key", "dummy-secret")))
                .endpointOverride(URI.create(dynamoEndpoint))
                .region(Region.of(region))
                .build();

    }

    /**
     * Provides an enhanced client.
     *
     * @param baseClient Client to wrap with enhanced client.
     * @return DynamoDbEnhancedClient
     */
    @Provides
    DynamoDbEnhancedClient provideEnhancedClient(DynamoDbClient baseClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(baseClient)
                .build();
    }

    /**
     * Provides an object used to interact with our table.
     *
     * @param dbClient  Enhanced client used to build our table instance.
     * @param tableName Name of our table.
     * @return DynamoDbTable
     */
    @Provides
    @Exposed
    DynamoDbTable<BasicEpisodeRecord> provideRecordTable(DynamoDbEnhancedClient dbClient,
                                                         @Named(EnvironmentModule.TABLE_NAME) String tableName) {
        DynamoDbTable<BasicEpisodeRecord> episodeTable =
                dbClient.table(tableName, TableSchema.fromBean(BasicEpisodeRecord.class));
        try {
            episodeTable.createTable();

            try (DynamoDbWaiter waiter = DynamoDbWaiter.create()) {
                ResponseOrException<DescribeTableResponse> response = waiter
                        .waitUntilTableExists(builder -> builder.tableName(tableName).build())
                        .matched();
                DescribeTableResponse tableDescription = response.response().orElseThrow(
                        () -> new RuntimeException("Customer table was not created."));
                // The actual error can be inspected in response.exception()
                System.out.println(tableDescription.table().tableName() + " was created.");
            }
        } catch (ResourceInUseException ex) {
            logger.info("Table {} already exists", tableName);
        }

        return episodeTable;
    }

}
