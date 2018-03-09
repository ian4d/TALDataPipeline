package com.ianford.tal;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileWriter;
import java.io.IOException;

public class RawDataDownloader {

    private final String urlFormat;
    private final String downloadDestinationFolder;
    private final String outputFileNameFormat;

    public RawDataDownloader(String urlFormat, String downloadDestinationFolder, String outputFileNameFormat) {
        this.urlFormat = urlFormat;
        this.downloadDestinationFolder = downloadDestinationFolder;
        this.outputFileNameFormat = outputFileNameFormat;
    }

    protected String generateURL(int episodeNum) {
        return String.format(urlFormat, episodeNum);
    }

    protected String getHTMLContent(String episodeURL) throws IOException {
        Document document = Jsoup.connect(episodeURL).get();
        String documentString = document.toString();
        return documentString;
    }

    protected String generateOutputFileDestination(int episodeNum) {
        return downloadDestinationFolder.concat(String.format(outputFileNameFormat, episodeNum));
    }

    protected void writeDocumentToOutputPath(String documentContent, String outputFileDestination) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFileDestination);
        fileWriter.write(documentContent);
    }

    public void downloadEpisode(int episodeNum) throws IOException {
        String episodeURL = generateURL(episodeNum);
        String documentContent = getHTMLContent(episodeURL);
        String outputPath = generateOutputFileDestination(episodeNum);
        writeDocumentToOutputPath(documentContent, outputPath);
    }
}
