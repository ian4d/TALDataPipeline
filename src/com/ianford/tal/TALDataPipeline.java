package com.ianford.tal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TALDataPipeline {

    // Command Operations
    public static final String COMMAND_PARSE_EPISODE = "parse-episode-number";
    public static final String COMMAND_UPDATE = "update";
    public static final String COMMAND_REBUILD = "rebuild";
    // Input Configuration
    public static final String INPUT_PATH = "input-path";
    public static final String INPUT_URL_FORMAT = "input-url-format";
    public static final String INPUT_FILENAME_FORMAT = "input-filename-format";
    // Output Configuration
    public static final String OUTPUT_PATH = "output-path";
    public static final String OUTPUT_FILENAME_FORMAT = "output-filename-format";
    private static final String DEFAULT_INPUT_URL_FORMAT = "http://www.thisamericanlife.com/%d/transcript";
    private static final String DEFAULT_INPUT_FILENAME_FORMAT = "episode-%d.html";
    private static final String DEFAULT_INPUT_PATH = "data/raw-html/";
    private static final String DEFAULT_OUTPUT_FILENAME_FORMAT = "episode-%d.csv";
    private static final String DEFAULT_OUTPUT_PATH = "data/episode-csv/";
    // Input Settings
    private final String inputURLFormat;
    private final String inputFilenameFormat;
    private final String inputPath;

    // Output Settings
    private final String outputPath;
    private final String outputFilenameFormat;

    public TALDataPipeline(final String inputURLFormat, final String inputFilenameFormat, final String inputPath, final String outputPath, final String outputFilenameFormat) {
        this.inputURLFormat = inputURLFormat;
        this.inputFilenameFormat = inputFilenameFormat;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.outputFilenameFormat = outputFilenameFormat;
    }

    public static void main(final String[] args) throws Exception {
        // Define Options
        final Options options = new Options();
        options.addOption("A", COMMAND_PARSE_EPISODE, true, "Episode number to try to parse");
        options.addOption("B", COMMAND_UPDATE, false, "Command to update and download latest episode");
        options.addOption("C", COMMAND_REBUILD, false, "Rebuild entire dataset");
        options.addOption("D", INPUT_PATH, true, "Path to download episodes to");
        options.addOption("E", OUTPUT_PATH, true, "Path to write parsed output to");
        options.addOption("F", INPUT_URL_FORMAT, true, "URL Format to use when acquiring episodes");
        options.addOption("G", OUTPUT_FILENAME_FORMAT, true, "Output filename format");
        options.addOption("H", INPUT_FILENAME_FORMAT, true, "Input filename format");

        // Parse Options
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, args);

        final String inputPath = Optional.of(cmd.getOptionValue(INPUT_PATH)).orElse(DEFAULT_INPUT_PATH);
        final String inputURLFormat = Optional.of(cmd.getOptionValue(INPUT_URL_FORMAT)).orElse(DEFAULT_INPUT_URL_FORMAT);
        final String inputFilenameFormat = Optional.of(cmd.getOptionValue(INPUT_FILENAME_FORMAT)).orElse(DEFAULT_INPUT_FILENAME_FORMAT);

        final String outputPath = Optional.of(cmd.getOptionValue(OUTPUT_PATH)).orElse(DEFAULT_OUTPUT_PATH);
        final String outputFilenameFormat = Optional.of(cmd.getOptionValue(OUTPUT_FILENAME_FORMAT)).orElse(DEFAULT_OUTPUT_FILENAME_FORMAT);

        final TALDataPipeline dataPipeline = new TALDataPipeline(inputURLFormat, inputFilenameFormat, inputPath, outputPath, outputFilenameFormat);

        if (cmd.hasOption(COMMAND_PARSE_EPISODE)) {
            final int episodeNumber = Integer.valueOf(cmd.getOptionValue(COMMAND_PARSE_EPISODE));
            dataPipeline.parseEpisode(episodeNumber);
            // If episode exists locally, parse it and write output
            // If episode does *not* exist locally, try to download it. If successful, parse and write.
            // If episode does not exist locally or remotely fail
        } else if (cmd.hasOption(COMMAND_UPDATE)) {
            // Identify missing episodes, run parse episode routine for each missing
        } else if (cmd.hasOption(COMMAND_REBUILD)) {
            // Run parse episode routine for all downloaded episodes
        }


        /**
         *
         * div.content
         *  article
         *      header
         *          h1 - #:Title
         *      div[content]
         *          div[act]
         *              h3 - Act name
         *              div[act-inner]
         *                  div[role]
         *                      h4 - speaker
         *                      p - content
         *
         *
         */

        // do some shit
        // configure my data pipeline instance with download/output path
        // inspect download path
    }

    /**
     * Parses the episode identified by the provided number
     *
     * @param episodeNumber
     * @throws IOException
     */
    public void parseEpisode(final int episodeNumber) throws Exception {

        final Map<String, String> roleToNameMap = new HashMap<>();
        final String episodeFilePath;
        if (!doesEpisodExistLocally(episodeNumber)) {
            // download the episode first
            if (doesEpisodeExistRemotely(episodeNumber)) {
                episodeFilePath = downloadEpisode(episodeNumber);
            } else {
                throw new Exception("Epsidoe does not exist");
            }
        } else {
            episodeFilePath = getEpisodeFilePath(episodeNumber);
        }

        final Document doc = Jsoup.parse(new File(episodeFilePath), Charset.defaultCharset().name());
        final Element contentDiv = doc.getElementById("content");

        final Element primaryArticle = contentDiv.getElementsByTag("article").first();
        final Element header = primaryArticle.getElementsByTag("header").first();
        final Element title = header.getElementsByTag("h1").first();
        final String titleContent = title.text();

        final Pattern pattern = Pattern.compile("(\\d+)\\:\\s(.*)");
        final Matcher matcher = pattern.matcher(titleContent);
        matcher.matches();
        final String episodeName = matcher.group(2);

        final Element divContent = primaryArticle.getElementsByClass("content").first();
        final Elements actList = divContent.getElementsByClass("act");

        final List<Record> recordList = new ArrayList<>();

        final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                .parseLenient()
                .appendPattern("HH:mm:ss.SS").toFormatter(Locale.US);

        int actNumber = 1;
        for (final Element act : actList) {
            final String actName = extractActName(act);
            for (final Element statement : extractActStatements(act)) {
                final String speakerRole = extractSpeakerRole(statement);
                final String speakerName = extractSpeakerName(statement);
                for (final Element paragraph : extractSpeakerStatements(statement)) {
                    final String startTime = extractStartTime(paragraph);
                    final String paragraphText = paragraph.text();

                    final Record record = new Record();
                    record.setActName(actName);
                    record.setActNumber(actNumber);
                    record.setEpisodeNumber(episodeNumber);
                    record.setEpisodeTitle(episodeName);
                    record.setSpeakerRole(Optional.ofNullable(speakerRole).orElse("NULL"));
                    record.setSpeakerName(Optional.ofNullable(speakerName).orElse("NULL"));
                    record.setStartTime(startTime);
                    record.setText(paragraphText);
                    record.setAirDate(null);
                    recordList.add(record);
                }
            }
            actNumber++;
        }

        for (final Record record : recordList) {
            System.out.println(record.toString());
        }

    }

    private String extractActName(final Element element) {
        final Elements actNameWrapper = element.getElementsByTag("h3");
        return actNameWrapper.isEmpty() ? null : actNameWrapper.first().text();
    }

    private Elements extractActStatements(final Element element) {
        final Element actInner = element.getElementsByClass("act-inner").first();
        return actInner.getElementsByTag("div");
    }

    private Elements extractSpeakerStatements(final Element element) {
        return element.getElementsByTag("p");
    }

    private String extractStartTime(final Element element) {
        return element.attr("begin");
    }

    private String extractSpeakerRole(final Element element) {
        return element.attr("class");
    }

    private String extractSpeakerName(final Element element) {
        final Elements speakerNameWrapper = element.getElementsByTag("h4");
        return speakerNameWrapper.isEmpty() ? null : speakerNameWrapper.first().text();
    }

    /**
     * Checks if we have a local copy of the requested episode
     *
     * @param episodeNumber
     * @return
     */
    public boolean doesEpisodExistLocally(final int episodeNumber) {
        return new File(getEpisodeFilePath(episodeNumber)).exists();
    }

    /**
     * Returns the file path to the episode data
     *
     * @param episodeNumber
     * @return
     */
    public String getEpisodeFilePath(final int episodeNumber) {
        return inputPath.concat(String.format(inputFilenameFormat, episodeNumber));
    }

    /**
     * Checks if the requested episode is present online
     *
     * @param episodeNumber
     */
    public boolean doesEpisodeExistRemotely(final int episodeNumber) {
        // generate expected URL for episode
        // ping website to find it
        return true;
    }

    /**
     * Identifies any missing local episodes
     *
     * @return
     */
    protected List<Integer> identifyMissingEpisodeNumbers() {
        final List<Integer> missingEpisodeNumbers = new ArrayList<>();
        final String[] fileList = new File(inputPath).list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.matches("episode\\-\\d.html");
            }
        });
        final int fileCount = fileList.length;
        for (int i = 1; i <= fileCount + 1; i++) {
            if (!doesEpisodExistLocally(i)) {
                missingEpisodeNumbers.add(i);
            }
            // see if an episode exists
        }
        return missingEpisodeNumbers;
    }

    protected String downloadEpisode(final int episodeNum) throws IOException {
        final String episodeURL = String.format(inputURLFormat, episodeNum);
        final Document document = Jsoup.connect(episodeURL).get();
        final String documentString = document.toString();

        final String outputPath = inputPath.concat(String.format(inputFilenameFormat, episodeNum));
        final FileWriter fileWriter = new FileWriter(outputPath);
        fileWriter.write(documentString);
        return outputPath;
    }


    /**
     * Saves the provided episode and returns the path to the generated file
     *
     * @param episodeNumber
     * @param episodeContents
     * @return
     * @throws IOException
     */
    protected String saveEpisode(final int episodeNumber, final String episodeContents) throws IOException {
        final String outputPath = inputPath.concat(String.format(inputFilenameFormat, episodeNumber));
        final FileWriter fileWriter = new FileWriter(outputPath);
        fileWriter.write(episodeContents);
        return outputPath;
    }
}
