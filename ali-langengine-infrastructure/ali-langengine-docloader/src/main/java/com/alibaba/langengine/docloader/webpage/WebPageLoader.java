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
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cuzz.lb
 * @date 2023/12/28 19:59
 */
@Slf4j
@Data
public class WebPageLoader extends BaseLoader {

    private static final int TIMEOUT_SECONDS = 30;

    private static volatile CloseableHttpClient httpClient = getHttpClient();

    private List<String> urlList;


    public WebPageLoader(String url) {
        this.urlList = Lists.newArrayList(url);
    }
    public WebPageLoader(String... urlArray) {
        this.urlList = Lists.newArrayList(urlArray);
    }

    public WebPageLoader(List<String> urlList) {
        this.urlList = urlList;
    }

    @Override
    public List<Document> load() {
        return doLoad();
    }

    private List<Document> doLoad() {
        List<Document> documents = Lists.newArrayList();
        urlList.forEach(url -> {
            try {
                Document document = loadWebPage(url);
                if (document != null) {
                    documents.add(document);
                    log.info("load web page success: {}", url);
                }
            } catch (IOException e) {
                log.error("load web page error: {}", url, e);
            }
        });
        return documents;
    }

    public Document loadWebPage(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        Document document = new Document();
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                String html = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                String content = getCleanContent(html,url);

                // Compute document ID based on content and URL
                String docId = computeSHA256(content + url);

                Map<String, Object> metaData = new HashMap<>();
                metaData.put("url", url);
                document.setUniqueId(docId);
                document.setPageContent(content);
                document.setMetadata(metaData);

            } else {
                throw new IOException("Server returned non-200 status code: " + response.getStatusLine().getStatusCode());
            }
        }
        return document;
    }

    private String getCleanContent(String html, String url) {
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        // 定义要排除的标签
        String[] tagsToExclude = {
                "nav", "aside", "form", "header", "noscript", "svg", "canvas", "footer", "script", "style"
        };
        // 定义要排除的ID
        String[] idsToExclude = {
                "sidebar", "main-navigation", "menu-main-menu"
        };
        // 定义要排除的类名
        String[] classesToExclude = {
                "elementor-location-header", "navbar-header", "nav", "header-sidebar-wrapper", "blog-sidebar-wrapper", "related-posts"
        };

        // 移除指定的标签
        for (String tag : tagsToExclude) {
            doc.select(tag).remove();
        }

        // 移除指定的ID
        for (String id : idsToExclude) {
            Elements elementsWithId = doc.select("#" + id);
            elementsWithId.remove();
        }

        // 移除指定的类
        for (String className : classesToExclude) {
            Elements elementsWithClass = doc.select("." + className);
            elementsWithClass.remove();
        }

        // 提取并返回清洁后的文本内容
        String content = doc.text();

        // Optionally, clean the content string of extra spaces, newlines, etc.
        content = cleanString(content);

        // 记录原始大小和清洁后的大小
        int originalSize = html.length();
        int cleanedSize = content.length();

        if (originalSize != 0) {
            log.info("[{}] Cleaned page size: {} characters, down from {} (shrunk: {} chars, {}%)", url,
                    originalSize, cleanedSize, originalSize - cleanedSize,
                    (1 - ((double) cleanedSize / originalSize)) * 100);
        }

        return content;
    }
    public String computeSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            log.error("Error computing SHA-256 hash", e);
            return null;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Implement cleanString if necessary, similar to clean_string in Python
    private String cleanString(String content) {
        // Remove extra whitespace, newlines, etc.
        return content.replaceAll("\\s+", " ").trim();
    }


    public static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (WebPageLoader.class) {
                if (httpClient == null) {
                    httpClient = createHttpClient();
                }
            }
        }
        return httpClient;
    }

    private static CloseableHttpClient createHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        // Create a local instance of cookie store
        CookieStore cookieStore = new BasicCookieStore();

        // Create request configuration with timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_SECONDS * 1000)
                .setConnectionRequestTimeout(TIMEOUT_SECONDS * 1000)
                .setSocketTimeout(TIMEOUT_SECONDS * 1000)
                .build();

        // Create the HTTP client using the cookie store and request configuration
        httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .build();
        return httpClient;
    }
    public static void closeHttpClient() {
        if (httpClient != null) {
            synchronized (WebPageLoader.class) {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        log.error("close http client error", e);
                    } finally {
                        httpClient = null;
                    }
                }
            }
        }
    }

}
