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
package com.alibaba.langengine.dashscope.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.dashscope.model.image.DashImageResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author chenshuaixin
 * @date 2024/05/17
 */
public class DashScopeImageTest {

    @Test
    public void testCreateImage() {
        DashScopeImage dashScopeImage = new DashScopeImage();
        dashScopeImage.setN(4);
        dashScopeImage.setRef_img("https://img.alicdn.com/imgextra/i1/O1CN01i5j8881hA8g6OdNRk_!!6000000004236-2-tps-168-112.png");
        dashScopeImage.setRef_strength(0.6f);
        dashScopeImage.setRef_mode("repaint");
        String json =dashScopeImage.run("生成一张相似的图", null, null, null);
        System.out.println(json);
        //{"output":{"task_id":"27ad6c1b-ef4a-46a8-bec3-10031963eb8e","task_status":"PENDING"},"request_id":"eb8a31a8-e87c-9fe7-a067-4c04d95fc93b"}
        DashImageResult dashImageResult= JSON.parseObject(json, DashImageResult.class);
        System.out.println(dashImageResult.getOutput().getTask_id());
        System.out.println(dashImageResult.getOutput().getTask_status());
        System.out.println(dashImageResult.getRequest_id());
        Assertions.assertEquals(true,false);
    }

    @Test
    public void testQueryImage(){
        DashScopeImage dashScopeImage=new DashScopeImage();
        String json=dashScopeImage.queryImage("657a9f91-6073-4a05-93fe-69b4fa6679a9");
        System.out.println(json);
        Assertions.assertEquals(true,false);
    }
}
