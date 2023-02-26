package com.ianford.podcasts.tal.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ianford.podcasts.model.EpisodeRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ContributorReport {

    private static final Logger logger = LogManager.getLogger();

    private final String outputDirectory;
    private final String contributor;
    private final Collection<EpisodeRecord> records;
    private final ObjectMapper mapper;
    private final Function<String, String> nameNormalizer;


    /**
     * Constructor
     *
     * @param outputDirectory
     * @param contributor
     * @param records
     * @param mapper
     * @param nameNormalizer
     */
    public ContributorReport(String outputDirectory, String contributor, Collection<EpisodeRecord> records,
                             ObjectMapper mapper,
                             Function<String, String> nameNormalizer) {
        this.outputDirectory = outputDirectory;
        this.contributor = contributor;
        this.records = records;
        this.mapper = mapper;
        this.nameNormalizer = nameNormalizer;
    }

    public void buildReport() {
        try {
            Set<String> episodeSet = new HashSet<>();
            List<String> allWords = new ArrayList<>();

            for (EpisodeRecord record : records) {
                episodeSet.add(String.valueOf(record.getEpisodeNumber()));
                String[] wordSplit = record.getText()
                        .split("\\W+");
                allWords.addAll(Arrays.asList(wordSplit));
            }
            Set<String> uniqueWords = new HashSet<>(allWords);
            ObjectNode node = mapper.createObjectNode();

            String normalizedName = nameNormalizer.apply(contributor);
            node.putPOJO("allWords", allWords);
            node.putPOJO("uniqueWords", uniqueWords);
            node.putPOJO("name", contributor);
            node.putPOJO("normalized-name", normalizedName);
            node.putPOJO("episodes", episodeSet);

            String filename = String.format(outputDirectory + "%s.json", normalizedName);
            logger.info("Writing report to {}", filename);
            mapper.writeValue(new File(filename), node);
        } catch (IOException e) {
            logger.error("Exception while writing contributor report", e);
        }


        // TODO: For the provided contributor, build and output a data file that contains
        /*
           name: A human readable name
           url: A version of the name normalized for a url
           episodes: A list of episodes the person has contributed to
           allWords: A list of every word the person has used
           wordsPerEpisode: A map of the number of words spoken by this contributor in each episode
           percentWordsPerEpisode: wordsPerEpisode as a percentage
         */
    }
}
