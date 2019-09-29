package com.ianford.podcasts.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Represents a single episode of some particular show
 */
@DynamoDbBean
public interface EpisodeRecordInterface {

    public String getShowName();

    public String getSort();

    public String getValue();
}
