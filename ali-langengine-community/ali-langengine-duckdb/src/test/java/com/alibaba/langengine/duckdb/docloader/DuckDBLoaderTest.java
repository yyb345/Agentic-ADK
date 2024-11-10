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
package com.alibaba.langengine.duckdb.docloader;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { DuckDBConfig.class })
public class DuckDBLoaderTest {

    @Resource
    private DuckDBLoader duckDBLoader;

    @Test
    public void test_load_csv() {
        // success
        String filePath = getClass().getClassLoader().getResource("dockdb/salary.csv").getPath();
        duckDBLoader.setSql(String.format("select * from read_csv_auto('%s')", filePath));
        List<Document> documentList = duckDBLoader.load();
        System.out.println(JSON.toJSONString(documentList));
    }

    @Test
    public void test_load_csv_faq() {
        // success
        String filePath = getClass().getClassLoader().getResource("duckdb/faq.csv").getPath();
        duckDBLoader.setSql(String.format("select * from read_csv_auto('%s')", filePath));
        List<Document> documentList = duckDBLoader.load();
        System.out.println(JSON.toJSONString(documentList));
    }

    @Test
    public void test_load_excel() {
        String filePath = getClass().getClassLoader().getResource("dockdb/formdemo.xlsx").getPath();
//        duckDBLoader.setSql(String.format("install spatial; load spatial; select * from st_read('%s', layer='Sheet1')", filePath));
        duckDBLoader.setSql(String.format("load spatial; select * from st_read('%s', layer='Sheet1')", filePath));
        List<Document> documentList = duckDBLoader.load();
        System.out.println(JSON.toJSONString(documentList));
    }

    @Test
    public void test_load_json() {
        String filePath = getClass().getClassLoader().getResource("dockdb/salary.json").getPath();
        duckDBLoader.setSql(String.format("select * from read_json_auto('%s') where team = 'a'", filePath));
        List<Document> documentList = duckDBLoader.load();
        System.out.println(JSON.toJSONString(documentList));
    }

    @Test
    public void test_load_parquet() {
        String filePath = getClass().getClassLoader().getResource("duckdb/demo.parquet").getPath();
        duckDBLoader.setSql(String.format("select * from read_parquet('%s')", filePath));
        List<Document> documentList = duckDBLoader.load();
        System.out.println(JSON.toJSONString(documentList));
    }

    @Test
    public void test_load_http_parquet() {
        String httpUrl = "http://localhost:7001/all";
//        duckDBLoader.setSql(String.format("install httpfs; load httpfs; select * from read_parquet('%s')", httpUrl));
        duckDBLoader.setSql(String.format("load httpfs; select * from read_parquet('%s')", httpUrl));
        List<Document> documentList = duckDBLoader.load();
        System.out.println(JSON.toJSONString(documentList));
    }
}
