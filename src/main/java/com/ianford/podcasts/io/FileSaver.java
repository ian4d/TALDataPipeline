package com.ianford.podcasts.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Writes output files
 */
@SuppressWarnings("unused")
public class FileSaver implements BiConsumer<String, String> {

    private final Logger logger = LogManager.getLogger(FileSaver.class);

    @Override
    public void accept(String documentContent, String outputFileDestination) {
        logger.info("Writing output to {}", outputFileDestination);
        try (java.io.FileWriter fileWriter = new java.io.FileWriter(outputFileDestination)) {
            fileWriter.write(documentContent);
        } catch (IOException e) {
            logger.error("Error writing file", e);
        }
    }
}
