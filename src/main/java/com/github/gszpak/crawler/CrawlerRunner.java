package com.github.gszpak.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.*;

public class CrawlerRunner implements Closeable {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ExecutorService finderExecutor = Executors.newFixedThreadPool(3);
    private final ExecutorService writerExecutor = Executors.newSingleThreadExecutor();
    private final Crawler crawler;

    public CrawlerRunner(String outputFilePath) throws IOException {
        UrlFinder urlFinder = new UrlFinder(finderExecutor);
        AsyncWriter asyncWriter = buildWriter(outputFilePath);
        crawler = new Crawler(urlFinder, asyncWriter);
    }

    private AsyncWriter buildWriter(String outputFilePath) throws IOException {
        File outputFile = new File(outputFilePath);
        Files.deleteIfExists(outputFile.toPath());
        return new AsyncWriter(writerExecutor, outputFile);
    }

    public CompletionStage<Void> fetchAndWriteForwardUrls(String inputFilePath) throws IOException {
        List<String> urls = Files.readAllLines(
                new File(inputFilePath).toPath(), Charset.defaultCharset());
        LOGGER.info("Fetching sub-urls for {} urls", urls.size());
        CompletableFuture[] stages = urls
                .stream()
                .map(crawler::getAndWriteUrlsFromWebPage)
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(stages);
    }

    public void close() throws IOException {
        LOGGER.info("Shutting down helper threads...");
        finderExecutor.shutdown();
        writerExecutor.shutdown();
        LOGGER.info("Finder and writer threads shut down successfully");
        crawler.close();
        LOGGER.info("Crawler closed successfully");
    }
}
