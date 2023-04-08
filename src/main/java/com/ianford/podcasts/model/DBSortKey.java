package com.ianford.podcasts.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum DBSortKey {

    /**
     * This key is used in combo with DBPartitionKey.PODCAST_NAME to store data like so:
     * - Partition: TAL
     * - Sort: EP#LATEST
     */
    LATEST_EPISODE("EP#LATEST",
                   "EP\\#LATEST"),
    // Sort Keys

    /**
     * Used in combo with DBPartitionKey.EPISODE_NUMBER to store data like so:
     * - Partition: TAL#EP1
     * - Sort: NAME
     */
    EPISODE_NAME("NAME",
                 "NAME"),
    /**
     * Used in combo with DBPartitionKey.EPISODE_NUMBER to store data like so:
     * - Partition: TAL#EP1
     * - Sort: STATEMENT_1#TIME_00:00:00.00
     */
    EPISODE_STATEMENT("STATEMENT_%s#TIME_%s",
                      "STATEMENT_(\\d+)\\#TIME_([\\d\\:\\.]+)"),

    /**
     * Used in combo with DBPartitionKey.EPISODE_NUMBER to store data like so:
     * - Partition: TAL#EP1
     * - Sort: ACT1#NAME
     */
    ACT_NAME("ACT_%s#NAME",
             "ACT_(\\d+)\\#NAME"),

    /**
     * Used in combo with DBPartitionKey.EPISODE_NUMBER to store data like so:
     * - Partition: TAL#EP1
     * - Sort: ACT1#STATEMENT1#TIME_00:00:00.00
     */
    ACT_STATEMENT("ACT_%s#STATEMENT_%s#TIME_%s",
                  "ACT_(\\d+)\\#STATEMENT_(\\d+)\\#TIME_([\\d\\:\\.]+)");

    private final String value;
    private final Pattern pattern;

    @SuppressWarnings("unused")
    DBSortKey(String value, String regExPattern) {
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
