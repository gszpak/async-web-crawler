package com.github.gszpak.crawler;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger();

    private static Options buildOptions() {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(true);
        options.addOption(output);
        return options;
    }

    private static void buildAndRun(Options options, String[] args)
            throws ParseException, IOException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        CrawlerRunner runner = new CrawlerRunner(outputFilePath);
        List<String> urls = Files.readAllLines(
                new File(inputFilePath).toPath(), Charset.defaultCharset());
        runner
                .fetchAndWriteForwardUrls(urls)
                .exceptionally(e -> {
                    LOGGER.info("Error while fetching URLs: {}", e.getMessage());
                    return null;
                })
                .toCompletableFuture()
                .join();
        runner.close();
    }

    public static void main(String[] args) throws IOException {
        Options options = buildOptions();
        try {
            buildAndRun(options, args);
            LOGGER.info("Finished, exiting");
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("web-crawler", options);
            System.exit(1);
        }
    }
}
