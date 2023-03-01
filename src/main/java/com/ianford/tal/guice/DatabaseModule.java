package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.ianford.podcasts.io.FileSaver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides utilities for interacting with our Database
 */
public class DatabaseModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring DatabaseModule");
    }


    @Provides
    @Exposed
    @Singleton
    FileSaver provideFileSaver() {
        return new FileSaver();
    }
}
