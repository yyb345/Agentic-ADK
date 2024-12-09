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
package com.alibaba.langengine.sqlite.memory.cache.mapper;

import com.alibaba.langengine.sqlite.memory.cache.ChatInfoDO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SqliteChatInfoMapper {

    @Select("select key, value " +
            "from chat_info " +
            "where key = #{key} limit 1")
    @Results(id = "chatInfoDO", value = {
            @Result(property="key",  column="key"),
            @Result(property="value",   column="value"),
    })
    ChatInfoDO selectOne(@Param("key") String key);

    @Insert("insert into chat_info(key,value)" +
            "values(#{key}, #{value})")
    Long insert(ChatInfoDO chatInfoDO);
}
