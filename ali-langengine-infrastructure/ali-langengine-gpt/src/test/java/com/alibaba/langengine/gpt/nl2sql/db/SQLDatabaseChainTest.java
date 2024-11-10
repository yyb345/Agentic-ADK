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
package com.alibaba.langengine.gpt.nl2sql.db;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import com.alibaba.langengine.openai.model.ChatOpenAI;
import org.junit.jupiter.api.Test;

public class SQLDatabaseChainTest {

    @Test
    public void test_run() {
        // success
        ChatOpenAI llm = new ChatOpenAI();

        SQLDatabase db = SQLDatabase.fromUri("jdbc:sqlite:/Users/xiaoxuan.lp/works/sources/xiaoxuan.lp/ali-langengine/ali-langengine-infrastructure/ali-langengine-gpt/src/test/resources/Chinook.db", "sqlite");
//        db.setSampleRowsSize(2);
//        db.setIncludeTables(Arrays.asList(new String[]{ "Track" }));

        SQLDatabaseChain sqlDatabaseChain = SQLDatabaseChain.fromLlm(llm, db);
//        sqlDatabaseChain.setReturnSql(true);
//        sqlDatabaseChain.setUseQueryChecker(true);
//        sqlDatabaseChain.setTopK(3);
        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("query", "How many employees are there?");
//        inputs.put("query", "现在有多少员工？");
//        inputs.put("query", "帮我查询下有多少个音乐家？");
        inputs.put("query", "How many albums by Michael Tilson Thomas & San Francisco Symphony?");
//        inputs.put("query", "迈克尔·蒂尔森·托马斯和旧金山交响乐团有多少张专辑？");
//        inputs.put("query", "What are some example tracks by composer Johann Sebastian Bach?");
//        inputs.put("query", "What are some example tracks by Bach?");
        Map<String, Object> response = sqlDatabaseChain.run(inputs);
        System.out.println("text:" + JSON.toJSONString(response.get("text")));
    }

    @Test
    public void test_run_sqlite_customTableInfo() {
        // success
        ChatOpenAI llm = new ChatOpenAI();

        SQLDatabase db = SQLDatabase.fromUri("jdbc:sqlite:/Users/xiaoxuan.lp/works/sources/xiaoxuan.lp/ali-langengine/langengine-docs/db/Topmisc.db", "sqlite");

        Map<String, String> customTableInfo = new HashMap<>();
        customTableInfo.put("Track", "CREATE TABLE Track (\n" +
                "    \"TrackId\" INTEGER NOT NULL, \n" +
                "    \"Name\" NVARCHAR(200) NOT NULL,\n" +
                "    \"Composer\" NVARCHAR(220),\n" +
                "    PRIMARY KEY (\"TrackId\")\n" +
                ")\n" +
                "/*\n" +
                "3 rows from Track table:\n" +
                "TrackId Name    Composer\n" +
                "1   For Those About To Rock (We Salute You) Angus Young, Malcolm Young, Brian Johnson\n" +
                "2   Balls to the Wall   None\n" +
                "3   My favorite song ever   The coolest composer of all time\n" +
                "*/");
        db.setCustomTableInfo(customTableInfo);

        SQLDatabaseChain sqlDatabaseChain = SQLDatabaseChain.fromLlm(llm, db);
        Map<String, Object> inputs = new HashMap<>();
//        inputs.put("query", "What are some example tracks by Bach?");
        inputs.put("query", "What are some sample repertoires with names including Bach?");
        Map<String, Object> response = sqlDatabaseChain.run(inputs);
        System.out.println("text:" + JSON.toJSONString(response.get("text")));
    }
}
