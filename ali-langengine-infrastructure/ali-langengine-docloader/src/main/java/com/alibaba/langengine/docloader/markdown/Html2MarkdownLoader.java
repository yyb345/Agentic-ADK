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
package com.alibaba.langengine.docloader.markdown;

import com.alibaba.langengine.core.docloader.BaseLoader;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Html2MarkdownLoader
 *
 * @author xiaoxuan.lp
 */
@Data
public class Html2MarkdownLoader extends BaseLoader {

    private String htmlContent;

    @Override
    public List<com.alibaba.langengine.core.indexes.Document> load() {
        List<com.alibaba.langengine.core.indexes.Document> documents = new ArrayList<>();
        com.alibaba.langengine.core.indexes.Document document = new com.alibaba.langengine.core.indexes.Document();
        document.setPageContent(convertHtmlToMarkdown(htmlContent));
        Map<String, Object> metadata = new HashMap<>();
        document.setMetadata(metadata);
        documents.add(document);
        return documents;
    }

    private String convertHtmlToMarkdown(String html) {
        Document doc = Jsoup.parse(html);
        StringBuilder markdown = new StringBuilder();

        convertNodes(doc.body(), markdown);

        return markdown.toString()
                .replaceAll("(\\n){3,}", "\n\n")
                .replace("\\xa0", "")
                .trim()
                ;
    }

    private void convertNodes(Element element, StringBuilder markdown) {
        for (org.jsoup.nodes.Node node : element.childNodes()) {
            if (node instanceof TextNode) {
                markdown.append(((TextNode) node).text().trim());
            } else if (node instanceof Element) {
                Element child = (Element) node;
                String tagName = child.tagName();

                if (tagName.equals("p") || tagName.equals("div")) {
                    convertNodes(child, markdown);
                    markdown.append("\n");
                } else if (tagName.equals("br")) {
                    markdown.append("\n");
                } else if (tagName.equals("h1")) {
                    markdown.append("# ");
                    convertNodes(child, markdown);
                    markdown.append("\n");
                } else if (tagName.equals("ul")) {
                    markdown.append("\n");
                    convertNodes(child, markdown);
                    markdown.append("\n");
                } else if (tagName.equals("li")) {
                    markdown.append("* ");
                    convertNodes(child, markdown);
                    markdown.append("\n");
                } else if (tagName.equals("span")) {
                    convertNodes(child, markdown);
                } else if(tagName.equals("a")) {
                    String href = child.attr("href");
                    if(!StringUtils.isEmpty(href)) {
                        if(href.indexOf("doc.htm") >= 0
                                || href.indexOf("docV3.htm") >= 0
                                || href.indexOf("api.htm") >= 0
                                || href.indexOf("support/index.htm#/knowledge/") >= 0) {
                            markdown.append("<" + href + ">");
                        } else {
                            Node aNode = child.childNodes().get(0);
                            String aText = "";
                            if(aNode instanceof TextNode) {
                                aText = ((TextNode) aNode).text().trim();
                            }
                            markdown.append(String.format("[%s](%s)", aText, href));
                        }
                    }
                } else if(tagName.equals("strong") || tagName.equals("b")) {
                    markdown.append("**");
                    convertNodes(child, markdown);
                    markdown.append("**");
                } else if(tagName.equals("img")) {
                    String src = child.attr("src");
                    if(!StringUtils.isEmpty(src)) {
                        if(src.indexOf("data:image") == -1) {
                            markdown.append("[](" + src + ")\n");
                        }
                    }
                } else {
                    convertNodes(child, markdown);
                }
            }
        }
    }
}
