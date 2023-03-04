package com.ianford.tal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ianford.tal.guice.DynamoDBModule;
import com.ianford.tal.guice.EnvironmentModule;
import com.ianford.tal.guice.EpisodeDataModule;
import com.ianford.tal.guice.PipelineModule;
import com.ianford.tal.guice.PropertiesModule;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

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
    public static void main(String[] args) throws ParseException {
        logger.info("Running pipeline");

        final Options options = new Options();
        options.addOption(Option.builder()
                .longOpt("config")
                .hasArg(true)
                .desc("Location of input config file")
                .build());

        // Parse Options
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, args);
        String configPath = Optional.ofNullable(cmd.getOptionValue("config"))
                .orElse("config.properties");
        Injector injector = configureInjector(configPath);
        injector.getInstance(Pipeline.class)
                .runPipeline();
        logger.info("Pipeline complete");
    }


    /**
     * Prepares the injector with the properties in our properties file.
     *
     * @param configPath Path where a configuration file can be found.
     */
    private static Injector configureInjector(String configPath) {
        logger.info("Configuring injector");
        return Guice.createInjector(
                new EnvironmentModule(),
                new PropertiesModule(configPath),
                new PipelineModule(),
                new EpisodeDataModule(),
                new DynamoDBModule()
        );
    }
}
