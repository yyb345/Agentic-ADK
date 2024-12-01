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
package com.alibaba.langengine.agentframework.delegation.cotexecutor.support;

import com.alibaba.langengine.agentframework.utils.FrameworkUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.agentframework.model.domain.CardConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @Author zhishan
 * @Date 2024-08-28
 */
@Slf4j
public class CardMessageFormat {
    public static JSONArray format(JSONArray cardConfig, Map<String, Object> mapInput) {
        log.info("start format message to card. cardConfig={}, mapInput={}",
                JSONObject.toJSONString(cardConfig), JSONObject.toJSONString(mapInput));
        if (mapInput == null || mapInput.isEmpty() || cardConfig == null || cardConfig.isEmpty()) {
            return null;
        }
        JSONObject request = new JSONObject(mapInput);
        try {
            return replaceAndFormat(cardConfig,request);
        } catch (Exception exp) {
            log.error("format message exception. cardConfig={}, msg={}",
                    JSONObject.toJSONString(cardConfig), request, exp);
            return null;
        }
    }

    public static JSONArray format(String cardConfig, JSONObject inputMessage) {
        log.info("start format message to card. cardConfig={}, mapInput={}",
                cardConfig, inputMessage);
        if (StringUtils.isEmpty(cardConfig)) {
            return null;
        }
        try {
            return replaceAndFormat(JSONArray.parseArray(cardConfig), inputMessage);
        } catch (Exception exp) {
            log.error("format message exception. cardConfig={}, msg={}", cardConfig, inputMessage, exp);
            return null;
        }
    }

    private static JSONArray replaceAndFormat(JSONArray cardConfig, JSONObject dataInput) {
        log.info("start format message to card. cardConfig={}, dataInput={}", cardConfig, dataInput.toString());
        JSONArray realCardConfig = FrameworkUtils.replaceArray(cardConfig, dataInput);
        JSONArray resultArr = new JSONArray();
        for (Object obj:realCardConfig) {
            CardConfig cardConfigDomain = JSON.parseObject(JSON.toJSONString(obj), CardConfig.class);
            if (cardConfigDomain.getProps() == null) {
                log.warn("promps is null. cardConfig={}", cardConfig);
                continue;
            }
            if ("normal".equals(cardConfigDomain.getProps().getType())) {
                // 不是列表直接返回即可
                resultArr.add(obj);
                continue;
            }
            if ("vertical".equals(cardConfigDomain.getProps().getType())) {
                Integer maxCount = cardConfigDomain.getProps().getMaxCount();
                if (cardConfigDomain.getProps().getListDataPath() instanceof List) {
                    List<Map<String, Object>> listDataPath = JSON.parseObject(JSON.toJSONString(cardConfigDomain.getProps().getListDataPath()), new TypeReference<List<Map<String, Object>>>() {
                    });
                    maxCount = listDataPath.size() > maxCount ? maxCount : listDataPath.size();

                    List<Map<String, Object>> listData = new ArrayList<>();
                    for (int i = 0; i < maxCount; i++) {
                        Map<String, Object> listDataPathItem = listDataPath.get(i);

                        Map<String, Object> listDataItem = new HashMap<>();

                        for (Map.Entry<String, Object> entry : cardConfigDomain.getProps().getItemDataPath().entrySet()) {
                            String labelKey = entry.getKey();
                            Object item = listDataPathItem.get(entry.getValue().toString());
                            listDataItem.put(labelKey, item);
                        }
                        listData.add(listDataItem);
                    }
                    cardConfigDomain.getProps().setList(listData);
                }
                resultArr.add(cardConfigDomain);
            }
        }
        log.info("finish format message to card. realCardConfig is " + JSONObject.toJSONString(realCardConfig));
        return resultArr;
    }
}
