package com.github.gszpak.crawler;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class AsyncWriter {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ExecutorService executorService;
    private final File outputFile;
    public AsyncWriter(ExecutorService executorService, File outputFile) throws IOException {
        this.executorService = executorService;
        this.outputFile = outputFile;
    }

    public CompletionStage<Void> asyncWrite(List<String> lines) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Thread {} writing {} lines to file",
                    Thread.currentThread().getId(), lines.size());
            try {
                FileUtils.writeLines(outputFile, lines, true);
                return null;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }

}
