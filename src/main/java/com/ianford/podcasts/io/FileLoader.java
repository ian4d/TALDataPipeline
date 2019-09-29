package com.ianford.podcasts.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Loads a resource file as a String
 */
public class FileLoader implements UnaryOperator<String> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public String apply(String filePath) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource(filePath))
                    .getFile());
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            return bufferedReader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (FileNotFoundException e) {
            logger.fatal("File wasn't found while attempting to load", e);
        }
        return null;
    }
}
