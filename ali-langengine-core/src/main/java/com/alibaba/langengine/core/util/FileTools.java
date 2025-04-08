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
package com.alibaba.langengine.core.util;

import java.io.File;
import java.net.URL;
import java.util.UUID;

public class FileTools {

    private static final String TEMP_IMAGE_FOLDER = "/Users/xiaoxuan.lp/";

    public  static File getFileFromUrl(String url) {
        try {
            File tempFolder = new File(TEMP_IMAGE_FOLDER);
            if (tempFolder.exists() != true) {
                tempFolder.mkdirs();
            }
            String suffix = url.substring(url.lastIndexOf("."));

            URL httpUrl = new URL(url);
            File file = new File(TEMP_IMAGE_FOLDER + "/" + UUID.randomUUID() + suffix);
            org.apache.commons.io.FileUtils.copyURLToFile(httpUrl, file);
            return file;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
