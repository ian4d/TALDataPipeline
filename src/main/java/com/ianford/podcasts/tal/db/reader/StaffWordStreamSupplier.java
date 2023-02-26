package com.ianford.podcasts.tal.db.reader;

import com.google.inject.name.Named;
import com.ianford.podcasts.model.EpisodeRecord;
import com.ianford.tal.guice.constants.NamedInjections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StaffWordStreamSupplier implements Supplier<Stream<String>> {

    private static final Logger logger = LogManager.getLogger();

    private final Supplier<Stream<String>> staffNameStreamSupplier;
    private final Function<String, Stream<EpisodeRecord>> staffRecordStreamFunction;
    private final Collection<String> blackList;

    public StaffWordStreamSupplier(
            @Named(NamedInjections.NAME_STREAM_SUPPLIER) Supplier<Stream<String>> staffNameStreamSupplier,
            @Named(NamedInjections.RECORD_STREAM_FUNCTION)
            Function<String, Stream<EpisodeRecord>> staffRecordStreamFunction,
            Collection<String> blackList) {
        this.staffNameStreamSupplier = staffNameStreamSupplier;
        this.staffRecordStreamFunction = staffRecordStreamFunction;
        this.blackList = blackList;
    }

    @Override
    public Stream<String> get() {
        Stream<String> staffNameStream = staffNameStreamSupplier.get();
        List<String> staffNameList = staffNameStream.collect(Collectors.toList());
        logger.info("Building staff word stream");

        return staffNameList.stream()
                .flatMap(staffRecordStreamFunction)
                .map(EpisodeRecord::getText)
                .map(str -> Arrays.asList(str.split("\\W")))
                .flatMap(List::stream)
                .filter(this::checkBlacklist);
    }

    private void logStaffName(String staffName) {
        logger.info("-- {}", staffName);
    }

    private boolean checkBlacklist(String word) {
        return !blackList.contains(word);
    }

}
