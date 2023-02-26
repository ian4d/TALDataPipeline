package com.ianford.tal.steps;

import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.tal.config.PropertiesProvider;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildEpisodeModelStep implements PipelineStep {


    private final PropertiesProvider propertiesProvider;
    private final Supplier<Stream<String>> episodeNumberStreamSupplier;
    private final Function<Integer, Stream<EpisodeRecord>> episodeRecordFunction;
    private final BiConsumer<String, String> fileSaver;

    /**
     * Constructor
     *
     * @param propertiesProvider
     * @param episodeNumberStreamSupplier
     * @param episodeRecordFunction
     * @param fileSaver
     */
    public BuildEpisodeModelStep(PropertiesProvider propertiesProvider,
                                 Supplier<Stream<String>> episodeNumberStreamSupplier,
                                 Function<Integer, Stream<EpisodeRecord>> episodeRecordFunction,
                                 BiConsumer<String, String> fileSaver) {
        this.propertiesProvider = propertiesProvider;
        this.episodeNumberStreamSupplier = episodeNumberStreamSupplier;
        this.episodeRecordFunction = episodeRecordFunction;
        this.fileSaver = fileSaver;
    }

    @Override
    public void run() {

        // Get all current episode numbers
        Stream<Integer> episodeNumberStream = episodeNumberStreamSupplier.get()
                .map(Integer::parseInt);

        // Compress all records down into list of Strings
        List<String> episodeRecordList = episodeNumberStream
                .flatMap(episodeRecordFunction)
                .map(record -> String.format("%s: %s", record.getSpeakerName(), record.getText()))
                .collect(Collectors.toList());

        // Write joined list of strings to output
        fileSaver.accept(String.join("\n", episodeRecordList), "episode-data.txt");
    }
}
