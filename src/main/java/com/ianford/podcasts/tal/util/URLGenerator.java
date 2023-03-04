package com.ianford.podcasts.tal.util;

import java.util.function.Function;

/**
 * Generates URLs.
 */
@SuppressWarnings("unused")
public class URLGenerator implements Function<Integer, String> {

    private final String urlFormat;

    /**
     * Constructor.
     *
     * @param urlFormat String format to use when generating URLs
     */
    public URLGenerator(String urlFormat) {
        this.urlFormat = urlFormat;
    }

    @Override
    public String apply(Integer episodeNum) {
        return String.format(urlFormat, episodeNum);
    }
}
