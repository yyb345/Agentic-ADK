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
package com.alibaba.langengine.vectorstore.tablestore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BeanConfiguration.class, TableStoreConfig.class })
@Slf4j
public class TableStoreDBTest {

    @Resource
    private TableStoreDB tableStoreDB;

    @Test
    public void test_similaritySearch() {
        List<Document> result = tableStoreDB.similaritySearch("接口taobao.traderates.get没有获取评价信息", 1);
        log.info(JSON.toJSONString(result));

        result = tableStoreDB.similaritySearch("alibaba.ascp.logistics.offline.send报错是什么原因？", 1);
        log.info(JSON.toJSONString(result));

        result = tableStoreDB.similaritySearch("alibaba.item.edit.schema.get", 1);
        log.info(JSON.toJSONString(result));
    }

    /**
     * 当用户创建TableStoreDB实例后修改embedding，且新的embedding生成向量维度与旧的不同时，会导致与TableStore索引向量字段维度冲突，写入和查询都会失败
     */
    @Test
    public void test_similaritySearchWithConflictDimension() {
        tableStoreDB.setDimension(1);
        try {
            tableStoreDB.similaritySearch("接口taobao.traderates.get没有获取评价信息", 1);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertEquals("the dimension of the vector field in tablestore is conflicted with that of the embeddings", e.getMessage());
        } finally {
            tableStoreDB.setDimension(TableStoreDB.getDimensionByCurrentEmbeddings(tableStoreDB.getEmbeddings()));
        }
    }

    @Test
    public void test_addDocuments() {
        List<Document> documents = new ArrayList<>();

        Document document = new Document();
        document.setPageContent("问题:接口taobao.traderates.get没有获取评价信息，追评后可以获取到评价信息了？\n" + "答案:您好；\n"
            + "在主评期限内买家没有评价订单，直接追评了，主评是系统默认评价了，所以追评后taobao.traderates.get 获取到了系统默认的评价信息了。");
        document.setUniqueId("4359");
        document.setIndex(1);
        documents.add(document);

        document = new Document();
        document.setPageContent("问题:alibaba.ascp.logistics.offline.send报错“周期购发货需要传期数 #MISSING_PERIOD”\n" + "答案:您好；\n" + "入参feature：seqNo=期数");
        document.setUniqueId("4283");
        document.setIndex(1);
        documents.add(document);

        document = new Document();
        document.setPageContent(
            "问题:用不同的token请求alibaba.item.edit.schema.get接口，返回的字段不一致\n" + "答案:Q:为什么用不同的token请求alibaba.item.edit.schema.get接口，返回的字段不一致，一个返回skuOuterId，一个返回sku_outerId\n"
                + "A:一个是天猫的返回，一个是淘宝的返回，信息是不一样的");
        document.setUniqueId("4262");
        document.setIndex(1);
        documents.add(document);

        tableStoreDB.addDocuments(documents);
    }

    /**
     * 当用户创建TableStoreDB实例后修改embedding，且新的embedding生成向量维度与旧的不同时，会导致与TableStore索引向量字段维度冲突，写入和查询都会失败
     */
    @Test
    public void test_addDocumentsWithConflictDimension() {
        List<Document> documents = new ArrayList<>();

        Document document = new Document();
        document.setPageContent("问题:接口taobao.traderates.get没有获取评价信息，追评后可以获取到评价信息了？\n" + "答案:您好；\n"
            + "在主评期限内买家没有评价订单，直接追评了，主评是系统默认评价了，所以追评后taobao.traderates.get 获取到了系统默认的评价信息了。");
        document.setUniqueId("4359");
        document.setIndex(1);
        documents.add(document);

        document = new Document();
        document.setPageContent("问题:alibaba.ascp.logistics.offline.send报错“周期购发货需要传期数 #MISSING_PERIOD”\n" + "答案:您好；\n" + "入参feature：seqNo=期数");
        document.setUniqueId("4283");
        document.setIndex(1);
        documents.add(document);

        document = new Document();
        document.setPageContent(
            "问题:用不同的token请求alibaba.item.edit.schema.get接口，返回的字段不一致\n" + "答案:Q:为什么用不同的token请求alibaba.item.edit.schema.get接口，返回的字段不一致，一个返回skuOuterId，一个返回sku_outerId\n"
                + "A:一个是天猫的返回，一个是淘宝的返回，信息是不一样的");
        document.setUniqueId("4262");
        document.setIndex(1);
        documents.add(document);

        tableStoreDB.setDimension(1);
        try {
            tableStoreDB.addDocuments(documents);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertEquals("the dimension of the vector field in tablestore is conflicted with that of the embeddings", e.getMessage());
        } finally {
            tableStoreDB.setDimension(TableStoreDB.getDimensionByCurrentEmbeddings(tableStoreDB.getEmbeddings()));
        }
    }
}
