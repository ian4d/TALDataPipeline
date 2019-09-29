package com.ianford.podcasts.model.db;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum DBPartitionKey {
    // Partition Keys

    /**
     * The PODCAST_NAME enum is used as a partition to store the latest episode number when used in combo with
     * DBSortKey.LATEST_EPISODE like so:
     * - Partition: TAL
     * - Sort: EP#LATEST
     */
    PODCAST_NAME("TAL",
                 "TAL"),

    /**
     * Episode specific data is stored using this primary key in combination with a variety of members of DBSortKey.
     */
    EPISODE_NUMBER("TAL#EP_%s",
                   "TAL\\#EP_(\\d+)");


    private final String value;
    private final Pattern pattern;

    @SuppressWarnings("unused")
    DBPartitionKey(String value, String regExPattern) {
        this.value = value;
        this.pattern = Pattern.compile(regExPattern);
    }

    public String getValue() {
        return value;
    }

    public String format(Object... args) {
        return String.format(value,
                             args);
    }

    /**
     * Indicates whether a specific value matches the structure of this key
     *
     * @param value The value to match
     * @return boolean
     */
    public boolean matches(String value) {
        return this.pattern.matcher(value)
                .matches();
    }

    /**
     * Indicates whether a specific value matches the structure of this key
     *
     * @param value The value to match
     * @return boolean
     */
    public Matcher matcher(String value) {
        return this.pattern.matcher(value);
    }

    /**
     * Returns the type of Key represented by the input
     *
     * @param keyValue
     * @return
     */
    public static Optional<DBSortKey> resolveKeyType(String keyValue) {
        return Arrays.stream(DBSortKey.values())
                .filter(key -> key.matches(keyValue))
                .findFirst();
    }
}
