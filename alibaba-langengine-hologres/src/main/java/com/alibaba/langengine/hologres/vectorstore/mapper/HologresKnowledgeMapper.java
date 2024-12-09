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
package com.alibaba.langengine.hologres.vectorstore.mapper;

import com.alibaba.langengine.hologres.vectorstore.KnowledgeDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * hologres知识库mapper
 *
 * @author xiaoxuan.lp
 */
@Mapper
public interface HologresKnowledgeMapper {

    @Select("select pm_approx_squared_euclidean_distance(content, #{query}) as distance, content_id, type, idx, row_content, metadata " +
            "from knowledge " +
            "where type = #{type} order by distance asc limit #{limit}")
    @Results(id = "knowledgeDO", value = {
            @Result(property="distance",  column="distance"),
            @Result(property="id",   column="id"),
            @Result(property="contentId",  column="content_id"),
            @Result(property="type", column="type"),
            @Result(property="idx", column="idx"),
            @Result(property="rowContent", column="row_content"),
    })
    List<KnowledgeDO> similaritySearch(@Param("query") String query,
                                       @Param("type") Integer type,
                                       @Param("limit") Integer limit);

    @Insert("insert into knowledge(content_id, type, content, idx, row_content, metadata)" +
            "values(#{contentId}, #{type}, #{content}, #{idx}, #{rowContent}, #{metadata})")
    @Options(useGeneratedKeys = true)
    Long insert(KnowledgeDO knowledgeDO);
}
