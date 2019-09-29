package com.ianford.podcasts.tal.file;

import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.podcasts.tal.io.TALEpisodeParser;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Used to build a list of Episode Records
 */
public class EpisodeRecordListBuilder implements Supplier<List<List<EpisodeRecord>>> {

    private final TALEpisodeParser talEpisodeParser;
    private final Supplier<List<File>> fileListBuilder;

    public EpisodeRecordListBuilder(TALEpisodeParser talEpisodeParser, Supplier<List<File>> fileListBuilder) {
        this.talEpisodeParser = talEpisodeParser;
        this.fileListBuilder = fileListBuilder;
    }


    @Override
    public List<List<EpisodeRecord>> get() {
        return buildFileList().stream()
                .map(talEpisodeParser)
                .collect(Collectors.toList());
    }

    /**
     * Builds a List of raw files to use to build our data model with
     */
    private List<String> buildFileList() {
        return fileListBuilder
                .get()
                .stream()
                .map(File::getPath)
                .collect(Collectors.toList());
    }


}
