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
 * @date 2023/12/29 11:38
 */
public class SitemapLoaderTest {

    @Test
    public void testLoadSitemapeList() throws Exception {
        // success
        SitemapLoader sitemapLoader = new SitemapLoader("https://www.xml-sitemaps.com/download/blog.cuzz.site-be833ea3d/sitemap.xml?view=1");
        List<Document> load = sitemapLoader.load();
        System.out.println("-------------------------------------");
        System.out.println(load.size());
        System.out.println("-------------------------------------");
        System.out.println(load);
    }
}
