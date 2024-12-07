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
package com.alibaba.langengine.adbpg.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BeanConfiguration.class, AdbPostgresConfig.class})
public class AdbPostgresDBTest {

    @Resource
    private AdbPostgresDB adbPostgresDB;

    @Test
    public void test_similaritySearch() {
        // success
        List<Document> result = adbPostgresDB.similaritySearch("接口taobao.traderates.get没有获取评价信息", 1);
        System.out.println(JSON.toJSONString(result));

        result = adbPostgresDB.similaritySearch("alibaba.ascp.logistics.offline.send报错是什么原因？", 1);
        System.out.println(JSON.toJSONString(result));

        result =adbPostgresDB.similaritySearch("alibaba.item.edit.schema.get", 1);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test_addDocuments() {
        // success
        List<Document> documents = new ArrayList<>();

        Document document = new Document();
        document.setPageContent("问题:接口taobao.traderates.get没有获取评价信息，追评后可以获取到评价信息了？\n" +
                "答案:您好；\n" +
                "在主评期限内买家没有评价订单，直接追评了，主评是系统默认评价了，所以追评后taobao.traderates.get 获取到了系统默认的评价信息了。");
        document.setUniqueId("4359");
        document.setIndex(1);
        documents.add(document);

        document = new Document();
        document.setPageContent("问题:alibaba.ascp.logistics.offline.send报错“周期购发货需要传期数 #MISSING_PERIOD”\n" +
                "答案:您好；\n" +
                "入参feature：seqNo=期数");
        document.setUniqueId("4283");
        document.setIndex(1);
        documents.add(document);

        document = new Document();
        document.setPageContent("问题:用不同的token请求alibaba.item.edit.schema.get接口，返回的字段不一致\n" +
                "答案:Q:为什么用不同的token请求alibaba.item.edit.schema.get接口，返回的字段不一致，一个返回skuOuterId，一个返回sku_outerId\n" +
                "A:一个是天猫的返回，一个是淘宝的返回，信息是不一样的");
        document.setUniqueId("4262");
        document.setIndex(1);
        documents.add(document);

        adbPostgresDB.addDocuments(documents);
    }
}
