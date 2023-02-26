package com.ianford.podcasts.tal.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Used to identify episodes in the fileset that haven't been downloaded yet
 */
public class MissingEpisodeFinder {

    private final Logger logger = LogManager.getLogger(MissingEpisodeFinder.class);
    private final String outputPath;
    private final Pattern pattern = Pattern.compile("episode-(\\d+).html");

    public MissingEpisodeFinder(String outputPath) {
        this.outputPath = outputPath;
    }

    public List<Integer> findMissingEpisodes() {
        logger.info("Finding missing episodes");
        // Get list of files from output path
        List<String> allEpisodes = Arrays.asList(
                Objects.requireNonNull(new File(outputPath).list((dir, name) -> pattern.matcher(name)
                        .matches())));

        // Convert all file names to episode numbers
        List<Integer> allEpisodeNumbers = allEpisodes.stream()
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        // Find the largest episode number present
        Integer maxValue = allEpisodeNumbers.stream()
                .max(Comparator.naturalOrder())
                .orElse(0);

        logger.info("Most recent episode number is {}", maxValue);

        List<Integer> epNumList = IntStream.range(1, maxValue)
                .boxed()
                .collect(Collectors.toList());
        epNumList.removeAll(allEpisodeNumbers);
        epNumList.stream()
                .peek(num -> logger.info("-- episode number {}", num));

        return epNumList.isEmpty() ?
                Collections.singletonList(maxValue + 1) :
                epNumList;
    }
}
