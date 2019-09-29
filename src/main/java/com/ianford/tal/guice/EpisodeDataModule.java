package com.ianford.tal.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ianford.podcasts.io.FileSaver;
import com.ianford.podcasts.io.JSoupDocumentLoader;
import com.ianford.podcasts.tal.file.EpisodeRecordListBuilder;
import com.ianford.podcasts.tal.io.TALEpisodeParser;
import com.ianford.podcasts.tal.util.EpisodeDownloader;
import com.ianford.podcasts.tal.util.MissingEpisodeFinder;
import com.ianford.podcasts.tal.util.OutputPathGenerator;
import com.ianford.podcasts.tal.util.URLGenerator;
import com.ianford.tal.steps.DownloadEpisodeStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class EpisodeDataModule extends PrivateModule {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        logger.info("Configuring EpisodeDataModule");
    }


    @Provides
    @Exposed
    EpisodeDownloader provideEpisodeDownloader(
            @Named("download.urlFormat") String urlFormat,
            @Named("download.destination") String downloadDestinationFolder,
            @Named("download.fileNameFormat") String downloadFileNameFormat) {
        Function<Integer, String> urlGenerator = new URLGenerator(urlFormat);
        Function<Integer, String> outputPathGenerator = new OutputPathGenerator(downloadDestinationFolder,
                downloadFileNameFormat);
        Predicate<String> existingFilePredicate = path -> new File(path).exists();
        BiConsumer<String, String> outputWriter = new FileSaver();

        return new EpisodeDownloader(urlGenerator, outputPathGenerator, existingFilePredicate, outputWriter);
    }

    @Provides
    @Exposed
    MissingEpisodeFinder provideMissingEpisodeFinder(
            @Named("download.destination") String downloadDestinationFolder) {
        return new MissingEpisodeFinder(downloadDestinationFolder);
    }

    @Provides
    @Singleton
    TALEpisodeParser provideTALParser() {
        return new TALEpisodeParser(new JSoupDocumentLoader());
    }

    /**
     * Proivdes a class that can be used to build a list of episode files stored on the file system
     *
     * @param fileNamePattern The pattern to use when matching against files to confirm that they should be included in
     *                        the list
     * @param filePath        A path to the location to acquire files from
     * @return
     */
    @Provides
    @Singleton
    Supplier<List<File>> provideFileListBuilder(@Named("download.fileNamePattern") String fileNamePattern,
                                                @Named("download.destination") String filePath) {
        FilenameFilter fileNameFilter = new FilenameFilter() {
            final private Pattern pattern = Pattern.compile(fileNamePattern);

            @Override
            public boolean accept(File dir, String name) {
                return pattern.matcher(name)
                        .matches();
            }
        };
        File[] fileList = new File(filePath).listFiles(fileNameFilter);
        return () -> Arrays.asList(Objects.requireNonNull(fileList));
    }

    /**
     * Provides a class that builds a list of existing episode records stored on the filesystem
     *
     * @param talEpisodeParser Parses an episode stored on the filesystem
     * @param fileListBuilder  Supplies a List<File> to parse
     * @return
     */
    @Exposed
    @Provides
    EpisodeRecordListBuilder provideEpisodeRecordListBuilder(TALEpisodeParser talEpisodeParser,
                                                             Supplier<List<File>> fileListBuilder) {
        return new EpisodeRecordListBuilder(talEpisodeParser, fileListBuilder);
    }



    /**
     * Provides a step that is used to download new episodes locally
     *
     * @param episodeDownloader    Downloads the latest episode of the show
     * @param missingEpisodeFinder Identifies missing episodes
     * @return
     */
    @Provides
    @Exposed
    DownloadEpisodeStep provideDownloadEpisodeStep(EpisodeDownloader episodeDownloader,
                                                   MissingEpisodeFinder missingEpisodeFinder) {
        return new DownloadEpisodeStep(episodeDownloader, missingEpisodeFinder);
    }

}
