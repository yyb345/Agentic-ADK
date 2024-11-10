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
package com.alibaba.langengine.core.memory.factory;


/**
 * @author liuchunhe.lch on 2023/9/2 11:50
 * well-meaning people get together do meaningful things
 **/
public class ConversationMemoryFactory {

    public static class Holder {
        public static ConversationMemoryFactory memoryFactory = new ConversationMemoryFactory();
    }

    public static ConversationMemoryFactory getInstance() {
        return Holder.memoryFactory;
    }

//    /**
//     * 初始化chatMemory
//     * @param baseCache
//     * @param sessionId
//     * @return
//     */
//    public BaseChatMemory init(BaseCache baseCache, String sessionId) {
//        if (baseCache instanceof TairCache) {
//           return new ConversationTairMemory((TairCache) baseCache,sessionId);
//        }else if(baseCache instanceof SqliteCache){
//            return  new ConversationSqliteMemory((SqliteCache) baseCache,sessionId);
//        }
//        throw new IllegalArgumentException("BaseChatMemory init fail");
//    }
}
