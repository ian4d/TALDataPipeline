package com.ianford.tal.steps;

import com.ianford.podcasts.io.FileLoader;
import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.tal.config.PropertiesProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExportContributorPagesStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final PropertiesProvider propertiesProvider;
    private final BiConsumer<String, String> fileSaver;
    private final FileLoader fileLoader;
    private final Function<String, Stream<EpisodeRecord>> staffRecordFunction;
    private final Supplier<Stream<String>> staffNameStreamSupplier;
    private final Function<String, String> staffNameNormalizer;

    /**
     * Constructor
     *
     * @param propertiesProvider      Used to access configuration properties
     * @param fileSaver               Used to write files to the output directory
     * @param fileLoader              Used to load files
     * @param staffRecordFunction     Used to acquire Streams of staff records
     * @param staffNameStreamSupplier Used to acquire a Stream of the name of every staff member
     * @param staffNameNormalizer     Used to normalize staff names
     */
    public ExportContributorPagesStep(PropertiesProvider propertiesProvider, BiConsumer<String, String> fileSaver,
                                      FileLoader fileLoader,
                                      Function<String, Stream<EpisodeRecord>> staffRecordFunction,
                                      Supplier<Stream<String>> staffNameStreamSupplier,
                                      Function<String, String> staffNameNormalizer) {
        this.propertiesProvider = propertiesProvider;
        this.fileSaver = fileSaver;
        this.fileLoader = fileLoader;
        this.staffRecordFunction = staffRecordFunction;
        this.staffNameStreamSupplier = staffNameStreamSupplier;
        this.staffNameNormalizer = staffNameNormalizer;
    }


    @Override
    public void run() {
        logger.info("Exporting Jekyll Pages");

        // Load Header Template
        String headerPath = propertiesProvider.apply("build.contributors.headerTemplatePath");
        String headerTemplate = fileLoader.apply(headerPath);

        // Load Filename Format
        String fileNameTemplate = propertiesProvider.apply("build.contributors.pageFileNameFormat");

        // Provides a stream of staff names
        List<String> staffNames = staffNameStreamSupplier.get()
                .collect(Collectors.toList());

        logger.info("Building Jekyll Post Templates for Contributors");
        String outputDirectory = propertiesProvider.apply("build.contributors.pageOutputDirectory");
        for (String staffName : staffNames) {
            String normalizedName = staffNameNormalizer.apply(staffName);
            String fileName = String.format(fileNameTemplate, normalizedName);
            String fullPath = String.format("%s%s%s", outputDirectory, File.separator, fileName);
            String header = String.format(headerTemplate, staffName, normalizedName);
            fileSaver.accept(header, fullPath);
        }
    }
}
