package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class DataFilterModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();
    public static final String EXCLUDED_CONTRIBUTOR_SET = "ExcludedContributorSet";

    @Override
    protected void configure() {
        logger.info("Configuring DataFilterModule");
    }

    @Provides
    @Exposed
    @Named(EXCLUDED_CONTRIBUTOR_SET)
    Set<String> providedExcludedContributorSet() {
        Set<String> set = new HashSet<>();
        return set;
    }
}
