package com.ianford.podcasts.model;

public enum DBKey {
    PARTITION("TAL"),
    LATEST_EPISODE("EP#LATEST"),
    EPISODE_NAME("EP%s#NAME"),
    ACT_NAME("EP%s#ACT%s#NAME"),
    SPEAKER_TEXT("EP%d#ACT%d#TIME%s#TEXT"),
    SPEAKER_NAME("EP%d#ACT%d#TIME%s#SPEAKER");

    private final String value;

    @SuppressWarnings("unused")
    DBKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String format(Object... args) {
        return String.format(value, args);
    }
}
