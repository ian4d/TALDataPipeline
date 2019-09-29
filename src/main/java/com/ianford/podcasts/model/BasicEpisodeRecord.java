package com.ianford.podcasts.model;


import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class BasicEpisodeRecord implements EpisodeRecordInterface {

    String showName;
    String sort;
    String value;

    public BasicEpisodeRecord() {
    }

    public BasicEpisodeRecord(String showName, String sort, String value) {
        this.showName = showName;
        this.sort = sort;
        this.value = value;
    }

    @DynamoDbPartitionKey
    public void setShowName(String showName) {
        this.showName = showName;
    }

    @DynamoDbSortKey
    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public String getShowName() {
        return showName;
    }


    public String getSort() {
        return sort;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "BasicEpisodeRecord{" +
                "showName='" + showName + '\'' +
                ", sort='" + sort + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
