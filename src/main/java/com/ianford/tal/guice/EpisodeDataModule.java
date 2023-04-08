package com.ianford.tal.guice;

import com.google.gson.Gson;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ianford.podcasts.io.FileSaver;
import com.ianford.podcasts.io.JSoupDocumentLoader;
import com.ianford.podcasts.tal.io.RawEpisodeParser;
import com.ianford.podcasts.tal.util.EpisodeDownloader;
import com.ianford.podcasts.tal.util.OutputPathGenerator;
import com.ianford.podcasts.tal.util.URLGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class EpisodeDataModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring EpisodeDataModule");
    }


    /**
     * Provides an object used to download raw HTML for episodes of the podcast.
     *
     * @param urlFormat                 Format to use when generating URLs.
     * @param downloadDestinationFolder Local path to store downloaded episodes in.
     * @param downloadFileNameFormat    String format to use when generating filenames.
     * @return EpisodeDownloader
     */
    @Provides
    @Exposed
    EpisodeDownloader provideEpisodeDownloader(
            @Named("download.urlFormat") String urlFormat,
            @Named("download.destination") String downloadDestinationFolder,
            @Named("download.fileNameFormat") String downloadFileNameFormat) {
        URLGenerator urlGenerator = new URLGenerator(urlFormat);
        OutputPathGenerator outputPathGenerator = new OutputPathGenerator(downloadDestinationFolder,
                                                                          downloadFileNameFormat);
        Predicate<String> existingFilePredicate = path -> new File(path).exists();
        FileSaver outputWriter = new FileSaver();

        return new EpisodeDownloader(urlGenerator,
                                     outputPathGenerator,
                                     existingFilePredicate,
                                     outputWriter);
    }

    /**
     * Provides an object that converts raw HTML of episodes into records to store in our DB.
     *
     * @return RawEpisodeParser
     */
    @Provides
    @Exposed
    @Singleton
    RawEpisodeParser provideRawEpisodeParser(Gson gson) {
        return new RawEpisodeParser(new JSoupDocumentLoader(),
                                    gson);
    }

}
