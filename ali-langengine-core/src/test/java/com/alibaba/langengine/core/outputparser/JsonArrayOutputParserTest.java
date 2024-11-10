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
package com.alibaba.langengine.core.outputparser;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author yushuo
 * @version JsonArrayOutputParserTest.java, v 0.1 2023年12月26日 10:55 yushuo
 */
public class JsonArrayOutputParserTest {

    @Test
    public void test() {
        // success
        JsonArrayOutputParser<User> jsonArrayOutputParser = new JsonArrayOutputParser<User>(User.class);

        //JSONArray objects = JSONArray.parseArray(mockData());
        //System.out.println(objects);
        List<User> parse = jsonArrayOutputParser.parse(mockData());
        System.out.println(parse);
    }

    //@Test
    //public void test2(){
        //JsonOutputParser<User>  jsonOutputParser = new JsonOutputParser<User>(User.class);
        //User parse = jsonOutputParser.parse(mockData());
        //System.out.println(parse);
    //}


    @Data
    @Accessors(chain = true)
    private static class User {
        private String name;
        private int age;
    }

    private static String mockData() {
        List<User> userList = Lists.newArrayList(
                new User()
                        .setAge(18)
                        .setName("煜硕"),
                new User()
                        .setAge(19)
                        .setName("Gary")
        );
        return JSON.toJSONString(userList);
    }
}