package com.ianford.podcasts.model.db;


import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class PodcastDBDBRecord implements PodcastDBRecordInterface {

    String primaryKey;
    String sort;
    String value;

    @SuppressWarnings("unused")
    public PodcastDBDBRecord() {
    }

    @SuppressWarnings("unused")
    public PodcastDBDBRecord(String primaryKey, String sort) {
        this.primaryKey = primaryKey;
        this.sort = sort;
    }

    @SuppressWarnings("unused")
    public PodcastDBDBRecord(String primaryKey, String sort, String value) {
        this.primaryKey = primaryKey;
        this.sort = sort;
        this.value = value;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    @DynamoDbPartitionKey
    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getSort() {
        return sort;
    }

    @DynamoDbSortKey
    public void setSort(String sort) {
        this.sort = sort;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BasicEpisodeRecord{" +
                "showName='" + primaryKey + '\'' +
                ", sort='" + sort + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
