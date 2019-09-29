package com.ianford.podcasts.tal.util;

import java.util.function.Function;

/**
 * Generates URLs
 */
public class URLGenerator implements Function<Integer, String> {

    private final String urlFormat;

    /**
     * Constructor
     *
     * @param urlFormat
     */
    public URLGenerator(String urlFormat) {
        this.urlFormat = urlFormat;
    }

    @Override
    public String apply(Integer episodeNum) {
        return String.format(urlFormat, episodeNum);
    }
}
