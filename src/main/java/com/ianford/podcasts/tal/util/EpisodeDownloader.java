package com.ianford.podcasts.tal.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EpisodeDownloader implements Function<Integer, String> {

    private static final Logger logger = LogManager.getLogger(EpisodeDownloader.class);

    private final Function<Integer, String> urlGenerator;
    private final Function<Integer, String> outputPathGenerator;
    private final Predicate<String> existingFilePredicate;
    private final BiConsumer<String, String> outputWriter;

    /**
     * Constructor
     *
     * @param urlGenerator
     * @param outputPathGenerator
     * @param existingFilePredicate
     * @param outputWriter
     */
    public EpisodeDownloader(Function<Integer, String> urlGenerator,
                             Function<Integer, String> outputPathGenerator,
                             Predicate<String> existingFilePredicate,
                             BiConsumer<String, String> outputWriter) {
        this.urlGenerator = urlGenerator;
        this.outputPathGenerator = outputPathGenerator;
        this.existingFilePredicate = existingFilePredicate;
        this.outputWriter = outputWriter;
    }

    /**
     * Downloads an episode at the provided URL
     *
     * @param episodeURL
     * @return
     * @throws IOException
     */
    private String getHTMLContent(String episodeURL) throws IOException {
        logger.info("Downloading episode from URL: {}", episodeURL);
        Document document = Jsoup.connect(episodeURL)
                .userAgent("Mozilla/5.0 (Windows NT 6.2; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0")
                .ignoreHttpErrors(true)
//                .validateTLSCertificates(true)
                .followRedirects(true)
                .timeout(60000)
                .get();
        return document.toString();
    }

    /**
     * Downloads the episode indicated by episodeNumber and returns a path to the resulting file
     *
     * @param episodeNumber The number of the episode to download
     * @return
     */
    @Override
    public String apply(Integer episodeNumber) {
        logger.info("Checking status for episode {}", episodeNumber);
        // If we already have the episode locally, just return a path to it
        String outputPath = outputPathGenerator.apply(episodeNumber);
        if (!existingFilePredicate.test(outputPath)) {
            try {
                String episodeURL = urlGenerator.apply(episodeNumber);
                String documentContent = getHTMLContent(episodeURL);
                outputWriter.accept(documentContent, outputPath);
            } catch (IOException e) {
                logger.error("Exception while downloading episode {}", episodeNumber, e);
                return null;
            }
        }
        logger.info("Episode {} downloaded to {}", episodeNumber, outputPath);
        return outputPath;
    }
}
