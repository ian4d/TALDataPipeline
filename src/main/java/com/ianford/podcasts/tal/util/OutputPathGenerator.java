package com.ianford.podcasts.tal.util;

import java.util.function.Function;

public class OutputPathGenerator implements Function<Integer, String> {

    private final String downloadDestinationFolder;
    private final String downloadedFileNameFormat;

    /**
     * Constructor
     *
     * @param downloadDestinationFolder Destination path to write downloaded files
     * @param downloadedFileNameFormat  Filename format to use for downloaded files
     */
    public OutputPathGenerator(String downloadDestinationFolder, String downloadedFileNameFormat) {
        this.downloadDestinationFolder = downloadDestinationFolder;
        this.downloadedFileNameFormat = downloadedFileNameFormat;
    }

    @Override
    public String apply(Integer episodeNum) {
        String fileName = String.format(downloadedFileNameFormat, episodeNum);
        return String.format("%s/%s", downloadDestinationFolder, fileName);
    }

}
