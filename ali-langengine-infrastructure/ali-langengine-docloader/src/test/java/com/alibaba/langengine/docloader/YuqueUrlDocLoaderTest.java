package com.alibaba.langengine.docloader;

import java.util.List;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.MarkdownTextSplitter;

import com.alibaba.langengine.docloader.yuque.YuqueUrlDocLoader;
import org.junit.jupiter.api.Test;

/**
 * @author aihe.ah
 * @time 2024/2/2
 * 功能说明：
 */
public class YuqueUrlDocLoaderTest {

    @Test
    public void testLoadWithSlug() {
        YuqueUrlDocLoader yuqueDocLoader = new YuqueUrlDocLoader("", 100l);
        yuqueDocLoader.setDocumentUrl("");

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
    }

    @Test
    public void testLoad() {
        YuqueUrlDocLoader yuqueDocLoader = new YuqueUrlDocLoader("", 100l);
        yuqueDocLoader.setDocumentUrl("");

        List<Document> documentList = yuqueDocLoader.load();
        System.out.println("document count:" + documentList.size());

        for (int i = 0; i < documentList.size(); i++) {
            Document document = documentList.get(i);
            System.out.println(document.getMetadata());
        }

        //Document document = documentList.get(0);
        //String pageContent = document.getPageContent();
        //System.out.println(document.getMetadata());
        //MarkdownTextSplitter markdownTextSplitter = new MarkdownTextSplitter();
        //markdownTextSplitter.setMaxChunkSize(800);
        //List<String> strings = markdownTextSplitter.splitText(pageContent);
        //for (String string : strings) {
        //    System.out.println(string);
        //    System.out.println("====================================");
        //}
    }
}