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
package com.alibaba.langengine.djl.embeddings;

import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * java原生实现Huggingface embeddings
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class GanymedeNilEmbeddings extends Embeddings {

    public GanymedeNilEmbeddings(String modelIdOrPath, Boolean remote) {
        Criteria.Builder<String, float[]> builder = Criteria.builder()
                .setTypes(String.class, float[].class);

        if(remote) {
            builder.optModelUrls("djl://ai.djl.huggingface.pytorch/" + modelIdOrPath);
        } else {
            builder.optModelPath(Paths.get(modelIdOrPath));
        }

        Criteria<String, float[]> criteria =  builder.optEngine("PyTorch")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .optProgress(new ProgressBar())
                .build();
        try {
            model = criteria.loadModel();
            predictor = model.newPredictor();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private ZooModel<String, float[]> model;

    private Predictor<String, float[]> predictor;

    @Override
    public String getModelType() {
        return "5";
    }

    @Override
    public List<Document> embedDocument(List<Document> documents) {
        return getLenSafeEmbeddings(documents, "document");
    }

    @Override
    public List<String> embedQuery(String text, int recommend) {
        Document document = new Document();
        document.setPageContent(text);
        return getLenSafeEmbeddings(Arrays.asList(new Document[] { document }), "query").stream()
                .map(e -> JSON.toJSONString(e.getEmbedding()))
                .collect(Collectors.toList());
    }

    private List<Document> getLenSafeEmbeddings(List<Document> documents, String textType) {
//        for (Document document : documents) {
//            try {
//                String questionText = document.getPageContent();
//                float[] floatArray = predictor.predict(questionText);
//                List<Double> doubleList = new ArrayList<>();
//                for (float floatValue : floatArray) {
//                    doubleList.add((double) floatValue);
//                }
//                document.setEmbedding(doubleList);
//            } catch (Throwable e) {
//                log.error("getLenSafeEmbeddings error", e);
//            }
//        }
//        return documents;
        try {
            List<float[]> list = predictor.batchPredict(documents.stream().map(Document::getPageContent).collect(Collectors.toList()));
            for(int i = 0; i < list.size(); i++) {
                List<Double> doubleList = new ArrayList<>();
                for (float floatValue : list.get(i)) {
                    doubleList.add((double) floatValue);
                }
                documents.get(i).setEmbedding(doubleList);
            }
        } catch (Throwable e) {
            log.error("getLenSafeEmbeddings error", e);
        }
        return documents;
    }

    public void close() {
        log.warn("finalize...");
        if(predictor != null) {
            predictor.close();
            predictor = null;
        }
        if(model != null) {
            model.close();
            model = null;
        }
    }
}
