package com.github.gszpak.crawler.async;

import org.asynchttpclient.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class UrlFinder {

    private final ExecutorService executorService;

    public UrlFinder(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public CompletionStage<List<String>> extractUrls(Response response) {
        if (response.getStatusCode() >= 400) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        return CompletableFuture.supplyAsync(
                () -> extractUrls(response.getResponseBody()),
                executorService);
    }

    private List<String> extractUrls(String html) {
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("a[href]");
        return links
                .stream()
                .map(element -> element.attr("href"))
                .collect(Collectors.toList());
    }
}
