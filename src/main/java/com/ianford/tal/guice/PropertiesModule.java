package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.ianford.tal.config.PropertiesProvider;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Used to provide properties read in from configuration files
 */
@SuppressWarnings("unused")
public class PropertiesModule extends PrivateModule {

    // Logger
    private static final Logger logger = LogManager.getLogger();

    // The path to the configuration file to load
    private final String configPath;

    // A map used internally to
    private Map<String, String> propertiesMap;

    /**
     * Constructor
     *
     * @param configPath The path to a properties file
     */
    public PropertiesModule(String configPath) {
        this.configPath = configPath;
    }

    /**
     * Reads in properties file and populates local data store to be used in properties provider
     */
    @Override
    protected void configure() {
        logger.info("Configuring PropertiesModule");
        try {
            Configurations configs = new Configurations();
            PropertiesConfiguration config = configs.properties(new File(configPath));
            logger.info("Configuring pipeline with properties:");
            Iterator<String> propertyKeys = config.getKeys();
            propertiesMap = new HashMap<>();
            while (propertyKeys.hasNext()) {
                String key = propertyKeys.next();
                String value = String.valueOf(config.getProperty(key));
                logger.info(() -> String.format("\t\"%s\" -> \"%s\"", key, value));

                propertiesMap.put(key, value);

                bindConstant().annotatedWith(Names.named(key))
                        .to(value);
                expose(Key.get(String.class, Names.named(key)));
            }
        } catch (ConfigurationException e) {
            logger.fatal("Failed to load configuration", e);
        }
    }

    /**
     * Provides a Function that can be used to retrieve properties
     */
    @Exposed
    @Provides
    @Singleton
    PropertiesProvider providePropertiesProvider() {
        return new PropertiesProvider(propertiesMap);
    }
}
