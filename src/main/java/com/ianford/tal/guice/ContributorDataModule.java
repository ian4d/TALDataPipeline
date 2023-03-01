package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.ianford.podcasts.io.FileLoader;
import com.ianford.podcasts.io.FileSaver;
import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.tal.config.PropertiesProvider;
import com.ianford.tal.guice.constants.NamedInjections;
import com.ianford.tal.steps.BuildContributorModelStep;
import com.ianford.tal.steps.ExportContributorPagesStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides classes and utilities used by many areas of the program to normalize contributor names
 */
public class ContributorDataModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring ContributorDataModule");
    }

    /**
     * Provides a function used to normalize contributor names
     *
     * @return Function<String, String>
     */
    @Provides
    @Named(NamedInjections.NAME_NORMALIZER)
    Function<String, String> provideNameNormalizer() {
        return str -> str.toLowerCase()
                .replaceAll("\\s", "-");

    }


    /**
     * Provides a step used to build the contributors model
     *
     * @param propertiesProvider        Provides properties
     * @param fileSaver                 Writes files locally
     * @param staffRecordStreamFunction Provides a stream of staff records
     * @param staffNameStreamSupplier   Provides a stream of staff names
     * @param nameNormalizer            Normalizes staff names for use in file paths
     * @return BuildContributorModelStep
     */
    @Provides
    @Exposed
    BuildContributorModelStep provideBuildContributorsModelStep(PropertiesProvider propertiesProvider,
                                                                FileSaver fileSaver,
                                                                @Named(NamedInjections.RECORD_STREAM_FUNCTION)
                                                                Function<String, Stream<EpisodeRecord>> staffRecordStreamFunction,
                                                                @Named(NamedInjections.NAME_STREAM_SUPPLIER)
                                                                Supplier<Stream<String>> staffNameStreamSupplier,
                                                                @Named(NamedInjections.NAME_NORMALIZER)
                                                                Function<String, String> nameNormalizer) {
        return new BuildContributorModelStep(propertiesProvider, fileSaver, staffRecordStreamFunction,
                staffNameStreamSupplier,
                nameNormalizer);
    }

    /**
     * Provides a step that exports static pages for contributors
     *
     * @param propertiesProvider        Provides configuration properties
     * @param fileSaver                 Writes files locally
     * @param staffRecordStreamFunction Provides a stream of staff records
     * @param staffNameStreamSupplier   Provides a stream of staff names
     * @return ExportContributorPageStep
     */
    @Provides
    @Exposed
    ExportContributorPagesStep provideExportContributorPagesStep(PropertiesProvider propertiesProvider,
                                                                 FileSaver fileSaver,
                                                                 FileLoader fileLoader,
                                                                 @Named(NamedInjections.RECORD_STREAM_FUNCTION)
                                                                 Function<String, Stream<EpisodeRecord>> staffRecordStreamFunction,
                                                                 @Named(NamedInjections.NAME_STREAM_SUPPLIER)
                                                                 Supplier<Stream<String>> staffNameStreamSupplier,
                                                                 @Named(NamedInjections.NAME_NORMALIZER)
                                                                 Function<String, String> staffNameNormalizer
    ) {
        return new ExportContributorPagesStep(propertiesProvider, fileSaver, fileLoader, staffRecordStreamFunction,
                staffNameStreamSupplier, staffNameNormalizer);
    }
}
