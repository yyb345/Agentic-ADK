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
package com.alibaba.langengine.core.model;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 响应收集器
 *
 * @author xiaoxuan.lp
 */
public class ResponseCollector {

    private boolean sseInc;
    private List<String> answerContentList = Lists.newArrayList();

    public ResponseCollector(Boolean sseInc) {
        this.sseInc = sseInc;
    }

    public void collect(String content) {
        answerContentList.add(content);
    }

    public String joining() {
        if(sseInc) {
            return String.join("", answerContentList);
        } else {
            if (answerContentList.isEmpty()) {
                return "";
            }
            return answerContentList.get(answerContentList.size() - 1);
        }
    }
}
