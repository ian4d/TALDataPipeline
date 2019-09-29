package com.ianford.tal.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ianford.podcasts.io.FileSaver;
import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.podcasts.tal.reports.ContributorReport;
import com.ianford.tal.config.PropertiesProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildContributorModelStep implements PipelineStep {

    private static final Logger logger = LogManager.getLogger();

    private final PropertiesProvider propertiesProvider;
    private final FileSaver fileSaver;
    private final Function<String, Stream<EpisodeRecord>> staffRecordFunction;
    private final Supplier<Stream<String>> staffNameStreamSupplier;
    private final Function<String, String> nameNormalizer;

    /**
     * Constructor
     *
     * @param propertiesProvider
     * @param fileSaver
     * @param staffRecordFunction
     * @param staffNameStreamSupplier
     * @param nameNormalizer
     */
    public BuildContributorModelStep(PropertiesProvider propertiesProvider, FileSaver fileSaver,
                                     Function<String, Stream<EpisodeRecord>> staffRecordFunction,
                                     Supplier<Stream<String>> staffNameStreamSupplier,
                                     Function<String, String> nameNormalizer) {
        this.propertiesProvider = propertiesProvider;
        this.fileSaver = fileSaver;
        this.staffRecordFunction = staffRecordFunction;
        this.staffNameStreamSupplier = staffNameStreamSupplier;
        this.nameNormalizer = nameNormalizer;
    }


    @Override
    public void run() {
        logger.info("Beginning Contributor Report");

        Stream<String> staffNameStream = staffNameStreamSupplier.get();
        String outputDirectory = propertiesProvider.apply("build.contributors.outputDirectory");
        ObjectMapper objectMapper = new ObjectMapper();

        List<String> staffList = staffNameStream.collect(Collectors.toList());
        for (String staffName : staffList) {
            logger.info("Building report for {}", staffName);
            ContributorReport report = new ContributorReport(
                    outputDirectory,
                    staffName,
                    staffRecordFunction.apply(staffName)
                            .collect(Collectors.toList()),
                    objectMapper, nameNormalizer);
            report.buildReport();
        }
        logger.info("Completing Contributor Report");
    }
}
