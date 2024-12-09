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
package com.alibaba.langengine.sqlite.memory.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.caches.BaseCache;
import com.alibaba.langengine.core.prompt.MessageInfoDO;

import com.alibaba.langengine.sqlite.memory.cache.mapper.SqliteChatInfoMapper;
import com.alibaba.langengine.sqlite.memory.cache.mapper.SqliteMessageInfoMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Cache that stores things in sqlite.
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class SqliteCache extends BaseCache {

    private SqliteChatInfoMapper sqliteChatInfoMapper;
    private SqliteMessageInfoMapper sqliteMessageInfoMapper;

    @Override
    public List<Generation> get(String prompt, String llmString) {
        String cacheKey = getCacheKey(prompt, llmString);
        ChatInfoDO chatInfoDO = sqliteChatInfoMapper.selectOne(cacheKey);
        if(chatInfoDO == null) {
            return null;
        }
        return JSON.parseArray(chatInfoDO.getValue(), Generation.class);
    }

    @Override
    public void update(String prompt, String llmString, List<Generation> returnVal) {
        String cacheKey = getCacheKey(prompt, llmString);
        ChatInfoDO chatInfoDO = new ChatInfoDO();
        chatInfoDO.setKey(cacheKey);
        chatInfoDO.setValue(JSON.toJSONString(returnVal));
        sqliteChatInfoMapper.insert(chatInfoDO);
    }

    @Override
    public void clear() {

    }

    public List<MessageInfoDO> getMessageInfo(String sessionId) {
        List<MessageInfoDO> messageInfoDOs = sqliteMessageInfoMapper.selectList(sessionId);
        return messageInfoDOs;
    }

    public void updateMessageInfo(String sessionId, String role, String content) {
        MessageInfoDO messageInfoDO = new MessageInfoDO();
        messageInfoDO.setSessionId(sessionId);
        messageInfoDO.setRole(role);
        messageInfoDO.setContent(content);
        sqliteMessageInfoMapper.insert(messageInfoDO);
    }

    public void remove(String sessionId) {
        Long num = sqliteMessageInfoMapper.delete(sessionId);
        log.warn("sqlite remove key:" + sessionId + " result num:" + num);
    }
}
