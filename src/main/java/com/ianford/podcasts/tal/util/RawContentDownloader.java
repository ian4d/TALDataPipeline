package com.ianford.podcasts.tal.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.function.UnaryOperator;

public class RawContentDownloader implements UnaryOperator<String> {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0";

    @Override
    public String apply(String episodeURL) {
        try {
            Document document = Jsoup
                    .connect(episodeURL)
                    .userAgent(DEFAULT_USER_AGENT)
                    .ignoreHttpErrors(true)
//                    .validateTLSCertificates(true)
                    .followRedirects(true)
                    .get();
            return document.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
