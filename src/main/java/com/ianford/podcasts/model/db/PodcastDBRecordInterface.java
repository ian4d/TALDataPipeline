package com.ianford.podcasts.model.db;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Represents a single episode of some particular show
 */
@SuppressWarnings("unused")
@DynamoDbBean
public interface PodcastDBRecordInterface {

    String getPrimaryKey();

    String getSort();

    String getValue();
}
