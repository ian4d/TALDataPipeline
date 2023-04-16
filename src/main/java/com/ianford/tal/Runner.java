package com.ianford.tal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ianford.tal.guice.DynamoDBModule;
import com.ianford.tal.guice.EnvironmentModule;
import com.ianford.tal.guice.EpisodeDataModule;
import com.ianford.tal.guice.PipelineModule;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Runs the application
 */
class Runner {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Static main method
     *
     * @param args Arguments passed into the method to configure the application
     */
    public static void main(String[] args) {
        logger.info("Running pipeline");


        // Parse Options
        Injector injector = configureInjector();
        injector.getInstance(Pipeline.class)
                .runPipeline();
        logger.info("Pipeline complete");
    }


    /**
     * Prepares the injector with the properties in our properties file.
     */
    private static Injector configureInjector() {
        logger.info("Configuring injector");
        return Guice.createInjector(new EnvironmentModule(),
                new PipelineModule(),
                new EpisodeDataModule(),
                new DynamoDBModule());
    }
}
