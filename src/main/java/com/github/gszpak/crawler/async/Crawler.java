package com.github.gszpak.crawler.async;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletionStage;

public class Crawler implements Closeable {

    private static final Logger LOGGER = LogManager.getLogger();

    private final UrlFinder urlFinder;
    private final AsyncHttpClient asyncHttpClient;
    private final AsyncWriter asyncWriter;

    public Crawler(UrlFinder urlFinder, AsyncWriter asyncWriter) {
        this.urlFinder = urlFinder;
        this.asyncHttpClient = Dsl.asyncHttpClient(
                Dsl.config().setIoThreadsCount(1).setFollowRedirect(true));
        this.asyncWriter = asyncWriter;
    }

    public CompletionStage<Void> getAndWriteUrlsFromWebPage(String url) {
        LOGGER.info("Fetching HTML for url: {}", url);
        BoundRequestBuilder getRequest = asyncHttpClient.prepareGet(url);
        ListenableFuture<Response> responseFuture = getRequest.execute();
        return responseFuture
                .toCompletableFuture()
                .thenApply(response -> {
                    LOGGER.info("Fetched HTML for url: {} using thread: {}",
                            url, Thread.currentThread().getId());
                    return response;
                })
                .thenCompose(urlFinder::extractUrls)
                .thenCompose(urls -> {
                    LOGGER.info("Url {}: found {} urls", url, urls.size());
                    return asyncWriter.asyncWrite(urls);
                });
    }

    @Override
    public void close() throws IOException {
        asyncHttpClient.close();
    }
}
