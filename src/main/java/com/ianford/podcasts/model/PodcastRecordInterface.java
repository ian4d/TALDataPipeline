package com.ianford.podcasts.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Represents a single episode of some particular show
 */
@SuppressWarnings("unused")
@DynamoDbBean
public interface PodcastRecordInterface {

    String getPrimaryKey();

    String getSort();

    String getValue();
}
