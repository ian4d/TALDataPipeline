package com.ianford.tal.steps;

import com.ianford.podcasts.io.FileSaver;
import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.tal.config.PropertiesProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds an NLP model using our DB
 */
public class BuildNLPModelStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final PropertiesProvider propertiesProvider;
    private final Supplier<Stream<String>> staffWordStreamSupplier;
    private final Function<String, Stream<EpisodeRecord>> staffRecordStreamFunction;
    private final Supplier<Stream<String>> staffNameStreamSupplier;

    private final Pattern pattern = Pattern.compile("[a-zA-Z]+");

    private final List<String> staffBlackList = Arrays.asList(
            "UNSPECIFIED_NAME",
            "Woman",
            "Woman 1",
            "Woman With Microphone",
            "Different Voices"
    );

    public BuildNLPModelStep(PropertiesProvider propertiesProvider,
                             Supplier<Stream<String>> staffWordStreamSupplier,
                             Function<String, Stream<EpisodeRecord>> staffRecordStreamFunction,
                             Supplier<Stream<String>> staffNameStreamSupplier) {
        this.propertiesProvider = propertiesProvider;
        this.staffWordStreamSupplier = staffWordStreamSupplier;
        this.staffRecordStreamFunction = staffRecordStreamFunction;
        this.staffNameStreamSupplier = staffNameStreamSupplier;
    }

    @Override
    public void run() {
        Stream<String> wordStream = staffWordStreamSupplier.get();
        List<String> wordStreamList = wordStream.collect(Collectors.toList());

        Map<String, Integer> wordCountMap = new HashMap<>();
        for (String word : wordStreamList) {
            addOrIncrementWord(wordCountMap, word);
        }

        List<String> wordList = new ArrayList<>(wordCountMap.keySet());
        wordList.sort(Comparator.comparing(wordCountMap::get));

        // TODO
        for (String word : wordList) {
            logger.info("\t{}: {}", word, wordCountMap.get(word));
        }

        // TODO: Trim resulting list down to top N elements
        List<String> finalOutput = new ArrayList<>();

        // TODO: Convert staff sentences into embedding
        List<String> staffNameList = staffNameStreamSupplier.get()
                .collect(Collectors.toList());
        staffNameList.removeAll(staffBlackList);
        for (String staffName : staffNameList) {
            List<EpisodeRecord> recordList = staffRecordStreamFunction.apply(staffName)
                    .collect(Collectors.toList());
            for (EpisodeRecord record : recordList) {
                String recordText = record.getText();
                List<String> sentences = parseSentences(recordText);
                for (String sentence : sentences) {
                    List<String> words = parseWords(sentence);
                    List<Integer> embedding = new ArrayList<>();
                    for (String word : words) {
                        int index = wordList.indexOf(normalizeWord(word));
                        if (index < 0) {
                            logger.info("Word not found: \"{}\"", word);
                        }
                        embedding.add(index);
                    }

                    if (Collections.max(embedding) >= 0) {
                        String result = String.format("%s, %s", staffName, embedding.toString());
                        finalOutput.add(result);
                    }

                }
            }
        }

        // TODO: Write embedding to some kind of datastore
        FileSaver fileSaver = new FileSaver();
        fileSaver.accept(String.join("\n", finalOutput), "corpus.txt");
    }


    /**
     * Breaks a record into sentences
     *
     * @param text
     * @return
     */
    private List<String> parseSentences(String text) {
        return Collections.singletonList(text);
    }

    /**
     * Breaks a sentence into words
     *
     * @param sentence
     * @return
     */
    private List<String> parseWords(String sentence) {
        return Arrays.asList(sentence.split("\\W"));
    }

    /**
     * Updates the count for each word
     *
     * @param wordMap
     * @param word
     */
    private void addOrIncrementWord(Map<String, Integer> wordMap, String word) {
        word = normalizeWord(word);
        if (pattern.matcher(word)
                .matches()) {
            wordMap.putIfAbsent(word, 0);
            wordMap.put(word, wordMap.get(word) + 1);
        }
    }


    private String normalizeWord(String word) {
        return word.trim()
                .toLowerCase();
    }

}
