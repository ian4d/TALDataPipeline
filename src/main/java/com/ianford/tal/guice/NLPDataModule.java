package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.tal.config.PropertiesProvider;
import com.ianford.tal.guice.constants.NamedInjections;
import com.ianford.tal.steps.BuildEpisodeModelStep;
import com.ianford.tal.steps.BuildNLPModelStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class NLPDataModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring NLPDataModule");
    }

    /**
     * Provides a step that's used to build our NLP model
     *
     * @param propertiesProvider   Provides values for named properties stored in a properties file
     * @param wordStreamSupplier   Provides a stream of words used by staffers
     * @param recordStreamFunction A Function that returns a stream of records for a staffer by name
     * @param nameStreamSupplier   Provides a stream of staff names
     * @return BuildNLPModelStep
     */
    @Provides
    @Exposed
    BuildNLPModelStep provideModelBuildingStep(
            PropertiesProvider propertiesProvider,
            @Named(NamedInjections.WORD_STREAM_SUPPLIER) Supplier<Stream<String>> wordStreamSupplier,
            @Named(NamedInjections.RECORD_STREAM_FUNCTION) Function<String, Stream<EpisodeRecord>> recordStreamFunction,
            @Named(NamedInjections.NAME_STREAM_SUPPLIER) Supplier<Stream<String>> nameStreamSupplier
    ) {
        return new BuildNLPModelStep(propertiesProvider, wordStreamSupplier, recordStreamFunction,
                nameStreamSupplier);
    }

    @Provides
    @Exposed
    BuildEpisodeModelStep provideEpisodeModelBuildingStep(
            PropertiesProvider propertiesProvider,
            @Named(NamedInjections.EPISODE_NUMBER_SUPPLIER) Supplier<Stream<String>> episodeNumberStreamSupplier,
            @Named(NamedInjections.EPISODE_RECORD_FUNCTION)
            Function<Integer, Stream<EpisodeRecord>> episodeRecordFunction,
            @Named(NamedInjections.FILE_SAVER) BiConsumer<String, String> fileSaver
    ) {
        return new BuildEpisodeModelStep(propertiesProvider, episodeNumberStreamSupplier, episodeRecordFunction,
                fileSaver);
    }

}
