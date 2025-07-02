package com.alibaba.langengine.docloader;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.MarkdownTextSplitter;
import com.alibaba.langengine.docloader.yuque.YuqueDocLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

public class YuqueDocLoaderTest {

    @Test
    public void test_load() {
        YuqueDocLoader yuqueDocLoader = new YuqueDocLoader("", 100l);
        yuqueDocLoader.setNamespace("");
        yuqueDocLoader.setLimit(5);
        yuqueDocLoader.setOptionalProperties("");

        List<Document> documentList = yuqueDocLoader.load();
        System.out.println("document count:" + documentList.size());
        System.out.println(JSON.toJSONString(documentList));
    }

    @Test
    public void test_load_slug() {
        YuqueDocLoader yuqueDocLoader = new YuqueDocLoader("", 100l);
        yuqueDocLoader.setNamespace("");
        yuqueDocLoader.setSlug("");

        List<Document> documentList = yuqueDocLoader.load();
        System.out.println("document count:" + documentList.size());
        Document document = documentList.get(0);
        String pageContent = document.getPageContent();
        System.out.println(document.getMetadata());
        MarkdownTextSplitter markdownTextSplitter = new MarkdownTextSplitter();
        markdownTextSplitter.setMaxChunkSize(800);
        List<String> strings = markdownTextSplitter.splitText(pageContent);
        for (String string : strings) {
            System.out.println(string);
            System.out.println("====================================");
        }
        //System.out.println(JSON.toJSONString(documentList));
    }
}
