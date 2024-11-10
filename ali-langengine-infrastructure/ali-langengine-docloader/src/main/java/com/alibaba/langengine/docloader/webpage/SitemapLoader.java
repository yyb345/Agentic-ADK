/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.docloader.webpage;

import com.alibaba.langengine.core.docloader.BaseLoader;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 这个跟WebPage相比，sitemap， 主要是把sitemap里面的url都加载下来，然后再加载页面
 *
 * @author cuzz.lb
 * @date 2023/12/29 10:53
 */
@Data
@Slf4j
public class SitemapLoader extends BaseLoader {

    private String sitemapSource;

    public SitemapLoader(String sitemapSource) {
        this.sitemapSource = sitemapSource;
    }

    @Override
    public List<com.alibaba.langengine.core.indexes.Document> load() {
        return loadSitemap(sitemapSource);
    }

    private List<com.alibaba.langengine.core.indexes.Document> loadSitemap(String sitemapSource) {
        List<com.alibaba.langengine.core.indexes.Document> result = new ArrayList<>();
        WebPageLoader webPageLoader = new WebPageLoader();
        String sitemapContent = getSitemapContent(WebPageLoader.getHttpClient(), sitemapSource);

        org.jsoup.nodes.Document nodeDoc = parseSitemap(sitemapContent);
        if (nodeDoc == null) {
            log.error("Failed to parse the sitemap.");
            return result;
        }

        Elements locElements = nodeDoc.select("url > loc");
        if (locElements.isEmpty()) {
            locElements = nodeDoc.select("loc");
        }
        List<String> links = locElements.eachText();


        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Future<com.alibaba.langengine.core.indexes.Document>> futures;
        futures = new ArrayList<>();
        for (String link : links) {
            futures.add(executor.submit(() -> webPageLoader.loadWebPage(link)));
        }

        for (Future<com.alibaba.langengine.core.indexes.Document> future : futures) {
            try {
                com.alibaba.langengine.core.indexes.Document data = future.get();
                if (data != null) {
                    data.getMetadata().put("sitemapSource", sitemapSource);
                    data.getMetadata().put("source", sitemapSource);
                    result.add(data);
                }
            } catch (Exception e) {
                log.error("Error loading page: {}", e.getMessage());
            }
        }

        executor.shutdown();

        return result;
    }

    private String getSitemapContent(CloseableHttpClient httpClient, String sitemapSource) {
        try {
            String xmlContent;
            if (sitemapSource.startsWith("http://") || sitemapSource.startsWith("https://")) {
                HttpGet request = new HttpGet(sitemapSource);
                xmlContent = httpClient.execute(request, httpResponse ->
                        EntityUtils.toString(httpResponse.getEntity()));
            } else {
                File file = new File(sitemapSource);
                if (file.exists() && file.isFile()) {
                    xmlContent = new Scanner(file).useDelimiter("\\A").next();
                } else {
                    log.error("Invalid sitemap source. Please provide a valid URL or local file path.");
                    return null;
                }
            }
            return xmlContent;
        } catch (IOException e) {
            log.error("Error fetching sitemap: {}", e.getMessage());
            return null;
        }
    }

    private org.jsoup.nodes.Document parseSitemap(String xmlContent) {
        if (StringUtils.isEmpty(xmlContent)) {
            return null;
        }
        return Jsoup.parse(xmlContent, "", org.jsoup.parser.Parser.xmlParser());
    }

}
