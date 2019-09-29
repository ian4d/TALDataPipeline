package com.ianford.tal.config;

import java.util.Map;
import java.util.function.Function;

public class PropertiesProvider implements Function<String, String> {

    private final Map<String, String> propertiesMap;

    /**
     * Constructor
     *
     * @param propertiesMap A Map from property name to value
     */
    public PropertiesProvider(Map<String, String> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

    @Override
    public String apply(String key) {
        return propertiesMap.get(key);
    }
}
