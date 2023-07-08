package com.ianford.tal.guice;

import com.google.gson.Gson;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.ianford.podcasts.io.JSoupDocumentLoader;
import com.ianford.tal.io.RawEpisodeParser;
import com.ianford.tal.util.EpisodeDownloader;
import com.ianford.tal.util.URLGenerator;
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
     * @return EpisodeDownloader
     */
    @Provides
    @Exposed
    @Singleton
    EpisodeDownloader provideEpisodeDownloader() {
        URLGenerator urlGenerator = new URLGenerator(System.getenv("TAL_URL_FORMAT"));
        Predicate<String> existingFilePredicate = path -> new File(path).exists();

        return new EpisodeDownloader(urlGenerator,
                System.getenv("TAL_LOCAL_FILENAME_FORMAT"),
                existingFilePredicate);
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
