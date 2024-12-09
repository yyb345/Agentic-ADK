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
package com.alibaba.langengine.docloader;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.docloader.markdown.Html2MarkdownLoader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.Test;

import java.util.List;

public class Html2MarkdownLoaderTest {

    @Test
    public void load() {
        // success
        Html2MarkdownLoader loader = new Html2MarkdownLoader();
//        loader.setHtmlContent("<div><span style=\"font-size: 12pt;\">你好；</span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">注意：请上传软件著作权登记证书扫描件； </span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">格式要求：jpg、png； </span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">大小要求：1M以下； </span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">尺寸：无要求。</span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\"><a href=\"https://img.alicdn.com/imgextra/i1/O1CN010Gvsm81MZgjUgRzJp_!!6000000001449-0-tps-2480-3505.jpg\" target=\"_blank\">https://img.alicdn.com/imgextra/i1/O1CN010Gvsm81MZgjUgRzJp_!!6000000001449-0-tps-2480-3505.jpg</a></span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">如何登记软件著作权：<a href=\"https://open.taobao.com/v2/doc?spm=a21n48.27062156.0.0.4af0286cWTC7Fb#/abilityToOpen?docType=1&amp;docId=121248\" target=\"_blank\">https://open.taobao.com/v2/doc?spm=a21n48.27062156.0.0.4af0286cWTC7Fb#/abilityToOpen?docType=1&amp;docId=121248</a></span></div>");
        loader.setHtmlContent("<div>您好，错误码 7 的产生是由于开放平台做的流控限制。目前平台有以下几种流控限制：</div>\n" +
                "<div>1、<strong>对每个应用（appkey）调用流量的限制</strong>，子错误码：accesscontrol.limited-by-app-access-count&nbsp; 。各应用标签对应流量见文档：<a draggable=\"false\" href=\"https://open.taobao.com/docV3.htm?docId=103492&amp;docType=1\" target=\"_blank\">https://open.taobao.com/docV3.htm?docId=103492&amp;docType=1</a>。应用具体的流量可在开发中心后台--证书管理中查看。</div>\n" +
                "<div>解决办法：<br />（1）现在提供了紧急流量重置功能，每天可以刷新三次，紧急情况可快速先恢复接口调用流量。流量重置后会清零重新统计。</div>\n" +
                "<div>步骤：登录<a href=\"https://console.open.taobao.com/?spm=a219a.7386653.1.20.3b89286cnaqscC#/index\" target=\"_blank\">开放平台控制台</a>--应用管理--选择对应应用管理--APP证书--&nbsp;点击 重置流量控制 ，就可以触发流量刷新。</div>\n" +
                "<div><img src=\"https://img.alicdn.com/top/i1/LB1yryKq.H1gK0jSZSyXXXtlpXa\" alt=\"\" width=\"797\" height=\"210\" />\n" +
                "<p><img src=\"https://img.alicdn.com/top/i1/LB1HdaLqYr1gK0jSZFDXXb9yVXa\" alt=\"\" width=\"799\" height=\"284\" /></p>\n" +
                "</div>\n" +
                "<div>（2）后续可在<a href=\"https://console.open.taobao.com/#/services/submitProblem\" target=\"_blank\">支持中心</a>提交工单，让技术支持小二申请更高流量。要求：\n" +
                "<ul>\n" +
                "<li>A . 最近7天应用访问API的平均成功率达到98%及以上；</li>\n" +
                "<li>B . 当前应用的有效访问总量未达到当前流量规则的50%，不允许提交申请；（比如您的应用当前流量阀值为2万次/天，如果您当前的有效访问总量在1万次/天以下，无法提交申请更高的流量）；</li>\n" +
                "<li>C .&nbsp;申请流程是逐级申请，只允许申请上一级别的流量。（比如您当前的流量规则为2万/天，更高的流量级别有10万和100万，那么您只能申请10万的流量包，不允许直接申请100万）。</li>\n" +
                "</ul>\n" +
                "</div>\n" +
                "<div>&nbsp;</div>\n" +
                "<div>\n" +
                "<div>2、<strong>对每个接口（api）调用总频率的限制</strong>，子错误码：accesscontrol.limited-by-api-access-count。</div>\n" +
                "<div>该限制是平台针对所有isv应用同时访问某个api接口每分钟或每秒钟调用总量的限制。是api级别的限制，与单个isv应用（appkey）无关，api级别流量限制目的是防止短时间内请求量过大影响后端服务的稳定性。</div>\n" +
                "<div>api限流报错，一般无法进行调整，只能稍后重试。此种错误一般还伴有&ldquo;This ban will last for ** more seconds&rdquo; 的信息，意思是等待**秒钟以后再调用。</div>\n" +
                "<div>解决办法：</div>\n" +
                "<div>建议ISV调整降低该接口调用频率，错开高峰期调用。或者使用其他可接口进行分流。</div>\n" +
                "</div>\n" +
                "<div>&nbsp;</div>\n" +
                "<div>3、<strong>对于未上线的应用，平台会限制该appkey调用某接口的频率</strong>，子错误码：accesscontrol.limited-by-app-api-access-count。</div>\n" +
                "<div>解决办法：接口业务方为了实现每个appkey资源合理分配，依照&ldquo;This ban will last for ** more seconds&rdquo; 中提示的限制时间结束后再调用。另外应用上线后会自动解除该限制。</div>");
//        loader.setHtmlContent("<div>\n" +
//                "<div><span style=\"font-size: 12pt;\">错误信息：</span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">\"code\":27,</span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">\"msg\":\"Invalid session\",</span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">\"sub_code\":\"invalid-sessionkey\",</span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">\"sub_msg\":\"非法或过期的SessionKey参数，请使用有效的SessionKey参数\"</span></div>\n" +
//                "</div>\n" +
//                "<div><span style=\"font-size: 12pt;\">请您确认以下几点情况是否存在：</span><br /><span style=\"font-size: 12pt;\">1、sessionkey过期：</span><br /><span style=\"font-size: 12pt;\">（1）用户取消授权或者sessionkey过期会提示 invalid-sessionkey.</span><br /><span style=\"font-size: 12pt;\">（2）sessionkey的有效时间与应用标签有关，详情请参见文档中&ldquo;安全等级&rdquo;和&ldquo;授权时长&rdquo;内容：https://open.taobao.com/doc.htm?docId=102635&amp;docType=1&amp;source=search#s4</span><br /><span style=\"font-size: 12pt;\">具体的授权时长可以使用 <a href=\"https://open.taobao.com/doc.htm?docId=109287&amp;docType=1\">商家授权时间查询工具</a>&nbsp;查看。</span><br /><span style=\"font-size: 12pt;\">（3）请解析获取到的top_parameter;</span><br /><span style=\"font-size: 12pt;\">A：如果是一般的sessionkey问题，请查看解析出来的expires_in字段的值（单位秒）；</span><br /><span style=\"font-size: 12pt;\">B：如果是高危API的sessionkey的问题，请查看hra_expires_in字段的值（单位秒）；</span><br /><span style=\"font-size: 12pt;\">2、如果您解析得到的内容包含：</span><br /><span style=\"font-size: 12pt;\">r1_expires_in&nbsp;,r2_expires_in,w1_expires_in,w2_expires_in，</span><br /><span style=\"font-size: 12pt;\">同时得到的四个值是1800的话请将您的APPKEY反馈给我们，</span><br /><span style=\"font-size: 12pt;\">3、店铺被冻结处罚了sessionkey也会无效，让用户在账号中心查看下账号状态。</span><br /><span style=\"font-size: 12pt;\">4、调接口使用的appkey 和 获取sessionkey 使用的appkey 不是同一个。</span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">5、检查是否使用了 <a href=\"https://open.taobao.com/doc.htm?docId=101618&amp;docType=1#s5\">ClusterTaobaoClient</a>，使用该类会导致sessionkey有时失效，过一会恢复正常，建议去掉ClusterTaobaoClient。</span><br /><span style=\"font-size: 12pt;\">6、如果是其他问题，请将您的验证过程和数据提交给我们。</span><br /><span style=\"font-size: 12pt;\">如何解析top_parameter请参考技术文档：</span><br /><span style=\"font-size: 12pt;\"><a href=\"http://open.taobao.com/doc/detail.htm?id=110\">http://open.taobao.com/doc/detail.htm?id=110</a></span><br /><span style=\"font-size: 12pt;\">7、\"sub_msg\":\"sessionkey-not-generated-by-server:RealSession don't belong app and user !\" 的错误；</span><br /><span style=\"font-size: 12pt;\">请通过开发者控制台进入应用详情，查看授权管理中是否有包含当前用户，</span><br /><span style=\"font-size: 12pt;\">可以通过taobao.user.seller.get获取user_id和错误提示中的user_id来比较确认。</span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">&nbsp;</span></div>\n" +
//                "<div><span style=\"font-size: 12pt;\">更多登录授权相关问题：<a href=\"https://open.taobao.com/doc.htm?docId=120&amp;docType=1\">https://open.taobao.com/doc.htm?docId=120&amp;docType=1</a></span></div>");
        List<Document> documentList = loader.load();
        String content = documentList.get(0).getPageContent();
//        System.out.println(content);

//        String finalContent = markdownToText(content);
//        System.out.println(finalContent);

        String text = convertHtmlToMarkdown(loader.getHtmlContent());
        System.out.println(text);
    }

    private String convertHtmlToMarkdown(String html) {
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        StringBuilder text = new StringBuilder();

        convertNodes(doc.body(), text);

        return text.toString()
                .replaceAll("(\\n){3,}", "\n")
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
                } else if(tagName.equals("strong") || tagName.equals("b")) {
                    convertNodes(child, markdown);
                } else if(tagName.equals("img")) {
                } else {
                    convertNodes(child, markdown);
                }
            }
        }
    }



//    public static String markdownToText(String markdownString) {
//        Parser parser = Parser.builder().build();
//        Node document = parser.parse(markdownString);
//        HtmlRenderer renderer = HtmlRenderer.builder().build();
//        String html = renderer.render(document);
//
//        // Remove code snippets
//        String cleanedHtml = html.replaceAll("<pre>(.*?)</pre>", " ");
//        cleanedHtml = cleanedHtml.replaceAll("<code>(.*?)</code>", " ");
//
//        // Extract text from HTML
//        org.jsoup.nodes.Document doc = Jsoup.parse(cleanedHtml);
//        String text = doc.text();
//
//        // Clean text
//        text = Jsoup.clean(text, "", Safelist.none(), new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
//        text = cleanText(text);
//
//        return text;
//    }
//
//    private static String cleanText(String text) {
//        // Clean HTML tags and URLs
//        String cleanedText = text.replaceAll("<.*?>", "");
//        cleanedText = cleanedText.replaceAll("(https?|ftp)://[^\\s/$.?#].[^\\s]*", "");
//        return cleanedText;
//    }
}
