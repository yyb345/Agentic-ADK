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
package com.alibaba.langengine.dashscope.embeddings;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.dashscope.embeddings.embedding.DashScopeConstant;
import com.alibaba.langengine.dashscope.embeddings.embedding.EmbeddingAsyncResult;
import com.alibaba.langengine.dashscope.embeddings.embedding.EmbeddingTaskStatus;
import org.junit.jupiter.api.Test;

public class DashScopeAsyncEmbeddingsTest {

    @Test
    public void test_embeddingAsyncResult() {
        // success
        DashScopeAsyncEmbeddings embeddings = new DashScopeAsyncEmbeddings();
        EmbeddingAsyncResult embeddingAsyncResult = embeddings.createEmbeddingsAsync("https://modelscope.oss-cn-beijing.aliyuncs.com/resource/text_embedding_file.txt", DashScopeConstant.TEXT_TYPE_DOCUMENT);
        System.out.println(JSON.toJSONString(embeddingAsyncResult));

        while (embeddingAsyncResult.getOutput().getTaskStatus().equals(EmbeddingTaskStatus.STATUS_PENDING)
            || embeddingAsyncResult.getOutput().getTaskStatus().equals(EmbeddingTaskStatus.STATUS_RUNNING)) {
            String taskId = embeddingAsyncResult.getOutput().getTaskId();
            embeddingAsyncResult = embeddings.getEmbeddingTask(taskId);
            System.out.println(JSON.toJSONString(embeddingAsyncResult));
        }
    }
}
