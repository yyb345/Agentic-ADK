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
package com.alibaba.langengine.docloader.webpage;

import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author cuzz.lb
 * @date 2023/12/29 10:41
 */
public class WebPageLoaderTest {

    @Test
    public void testLoadWebPage() throws Exception {
        // success
        WebPageLoader webPageLoader = new WebPageLoader("http://blog.cuzz.site/");
        List<Document> result = webPageLoader.load();
        System.out.println(result);
    }

    @Test
    public void testLoadWebPageList() throws Exception {
        // success
        WebPageLoader webPageLoader = new WebPageLoader("http://blog.cuzz.site/2022/03/19/%E6%AF%94%E7%89%B9%E5%B8%81%E5%8E%9F%E7%90%86/#more","http://blog.cuzz.site/2021/08/14/ThreadLocal%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90/#more");
        List<Document> result = webPageLoader.load();
        System.out.println(result);
    }


}
