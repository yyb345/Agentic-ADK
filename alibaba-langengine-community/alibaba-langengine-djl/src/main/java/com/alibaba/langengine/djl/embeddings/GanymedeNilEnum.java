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

import java.util.HashMap;
import java.util.Map;

/**
 * GanymedeNil模型枚举
 *
 * @author xiaoxuan.lp
 */
public enum GanymedeNilEnum {

    TEXT2VEC_LARGE_CHINESE(10001, "GanymedeNil/text2vec-large-chinese"),
    ;

    int code;

    String modelId;

    static Map<Integer, GanymedeNilEnum> codeToEnum;

    static {
        codeToEnum = new HashMap<Integer, GanymedeNilEnum>();
        for (GanymedeNilEnum typeInstance : values()) {
            codeToEnum.put(typeInstance.code, typeInstance);
        }
    }

    public static GanymedeNilEnum getEnumByCode(Number number) {
        if (number == null) {
            return null;
        }
        int intValue = number.intValue();
        return codeToEnum.get(intValue);
    }

    public static GanymedeNilEnum getEnum(String modelId) {
        GanymedeNilEnum[] sentenceTransformerEnums = GanymedeNilEnum.values();
        for (GanymedeNilEnum sentenceTransformerEnum : sentenceTransformerEnums) {
            if(sentenceTransformerEnum.getModelId().equals(modelId)) {
                return sentenceTransformerEnum;
            }
        }
        return null;
    }

    private GanymedeNilEnum(int code, String modelId) {
        this.code = code;
        this.modelId = modelId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
}
