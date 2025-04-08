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
package com.alibaba.langengine.core.docloader;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TextDocLoaderTest {

    @Test
    public void loadWithTxt() {
        // success
        String filePath = getClass().getClassLoader().getResource("example_data/information.txt").getPath();
        TextDocLoader loader = new TextDocLoader();
        loader.setFilePath(filePath);
        List<Document> documentList = loader.load();
        System.out.println(JSON.toJSONString(documentList));
    }
}
