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
package com.alibaba.langengine.core.textsplitter.code;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.MarkdownTextSplitter;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HtmlTextSplitterTest {

    @Test
    public void test_createDocuments() {
        // success
        HtmlTextSplitter textSplitter = new HtmlTextSplitter();
        textSplitter.setMaxChunkSize(60);
        textSplitter.setMaxChunkOverlap(0);
        textSplitter.setSeparatorRegex(true);
        String text = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>LangChain</title>\n" +
                "        <style>\n" +
                "            body {\n" +
                "                font-family: Arial, sans-serif;\n" +
                "            }\n" +
                "            h1 {\n" +
                "                color: darkblue;\n" +
                "            }\n" +
                "        </style>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <div>\n" +
                "            <h1>LangChain</h1>\n" +
                "            <p>⚡ Building applications with LLMs through composability ⚡</p>\n" +
                "        </div>\n" +
                "        <div>\n" +
                "            As an open source project in a rapidly developing field, we are extremely open to contributions.\n" +
                "        </div>\n" +
                "    </body>\n" +
                "</html>";
        List<Document> documents = textSplitter.createDocuments(Arrays.asList(new String[] { text }), new ArrayList<>());
        System.out.println(JSON.toJSONString(documents));
    }

    /**
     * HTML 分词，将一个大文本，按段落分出  title->content
     * 提升Embedding对于最大token的限制
     * <p>
     * 通过TextSplitter模块（文档介绍）把文本内容进行分词，形成一些文本段落，这样做的好处是提升Embedding对于最大token的限制；====== langengine提供了一些基础的，通过分段落、分句子、正则的基础都提供了
     */
    @Test
    public void test_createDocuments2() {
        // success
        List<String> htmlList = Lists.newArrayList();
        htmlList.add("<div lang=\"zh-CN\" class=\"icms-help-docs-content\">\n" + "<main id=\"f9015a3067gj5\"><p data-tag=\"shortdesc\" id=\"f9048e8167yc6\" class=\"shortdesc\"></p><div data-tag=\"conbody\" class=\"conbody\"><h4 data-tag=\"h4\" id=\"f9048e8267n3y\" class=\"h4\">产品介绍</h4><p id=\"0ee0428067c91\"><span style=\"color:rgb(13, 26, 38)\">蚂蚁链版权保护平台，是蚂蚁链旗下版权服务平台，该平台依托区块链、DNA溯源、AIOT等创新技术，为作品内容生产机构或内容运营企业提供集版权确权、权益存证、全网监测、链上取证为一体的一站式线上版权保护解决方案，以专业可信、科技普惠的产品设计理念帮助每一个原创作品获得尊重和价值。</span></p><h4 data-tag=\"h4\" id=\"f9048e8467cnw\" class=\"h4\">产品特点</h4><ul id=\"f904b59067ypp\"><li id=\"f904b5916799s\"><p id=\"33cdd7b067kkf\"><span style=\"color:rgb(13, 26, 38)\">全链路上链可信：打造司法可信环境，根据版权业务流程全流程上链，保证完整性和可信性</span></p></li><li id=\"f904b59367ymi\"><p id=\"3925a3f067dcj\"><span style=\"color:rgb(13, 26, 38)\">全网监测精准：毫秒间亿级检索能力，全网内快速精确地识别版权侵权行为</span></p></li><li id=\"f904b595679t4\"><p id=\"3e1f239067app\"><span style=\"color:rgb(13, 26, 38)\">全面的取证工具：基于具有自主知识产权的链上取证技术，通过网页截图取证、视</span><span style=\"color:rgb(53, 53, 53)\">频录屏取证、全自动化取证等多种在线取证方式，为维权提供可信赖的司法证据</span></p></li><li id=\"3f162dc167xor\"><p id=\"43bd209067ria\"><span style=\"color:rgb(13, 26, 38)\">DCI申领：基于中国版保中心提出的DCI体系3.0标准，帮助原创作者便捷获得DCI权属确认。同时数字作品和DCI会上链存证，具备内容不可篡改、时间不可篡改的特性，支持区块链核验</span></p></li></ul></div></main>\n" + "\n" + "\n" + "</div>");

        htmlList.add("<div class=\"测试\">/<div>");

        List<Document> documents = htmlList.stream().map(yuqueDocInfo -> {
            Document document = new Document();
            document.setUniqueId("uniqueId" + 1);
            document.setPageContent(yuqueDocInfo);
            return document;
        }).filter(e -> e != null).collect(Collectors.toList());

        List<Document> newDocuments;
        if (true) {
            HtmlTextSplitter textSplitter = new HtmlTextSplitter();
            newDocuments = textSplitter.createDocuments(documents.stream().map(document -> document.getPageContent()).collect(Collectors.toList()), null);
        } else {
            MarkdownTextSplitter textSplitter = new MarkdownTextSplitter();
            newDocuments = textSplitter.createDocuments(documents.stream().map(document -> document.getPageContent()).collect(Collectors.toList()), null);
            ;
        }
        System.out.println(JSON.toJSONString(newDocuments));
    }
}
