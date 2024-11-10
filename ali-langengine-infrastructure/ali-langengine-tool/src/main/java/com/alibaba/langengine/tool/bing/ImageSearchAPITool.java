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
package com.alibaba.langengine.tool.bing;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.tool.bing.service.BingService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;

import static com.alibaba.langengine.tool.ToolConfiguration.BING_API_KEY;
import static com.alibaba.langengine.tool.ToolConfiguration.BING_SERVER_URL;

/**
 * Bing Image Search工具
 * 参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-image-search/quickstarts/rest/java
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class ImageSearchAPITool extends DefaultTool {

    private BingService service;

    private String token = BING_API_KEY;

    /**
     * Filter images by the following license types:
     * Any — Return images that are under any license type. The response doesn't include images that do not specify a license or the license is unknown.
     * Public — Return images where the creator has waived their exclusive rights, to the fullest extent allowed by law.
     * Share — Return images that may be shared with others. Changing or editing the image might not be allowed. Also, modifying, sharing, and using the image for commercial purposes might not be allowed. Typically, this option returns the most images.
     * ShareCommercially — Return images that may be shared with others for personal or commercial purposes. Changing or editing the image might not be allowed.
     * Modify — Return images that may be modified, shared, and used. Changing or editing the image might not be allowed. Modifying, sharing, and using the image for commercial purposes might not be allowed.
     * ModifyCommercially — Return images that may be modified, shared, and used for personal or commercial purposes. Typically, this option returns the fewest images.
     * All — Do not filter by license type. Specifying this value is the same as not specifying the license parameter.
     */
    private String license = "public";

    /**
     * Filter images by the following image types:
     * AnimatedGif — Return animated gif images.
     * AnimatedGifHttps — Return animated gif images that are from an HTTPS address.
     * Clipart — Return only clip art images.
     * Line — Return only line drawings.
     * Photo — Return only photographs (excluding line drawings, animated gifs, and clip art).
     * Shopping — Return only images that contain items where Bing knows of a merchant that is selling the items. This option is valid in the en-US market only.
     * Transparent — Return only images with a transparent background.
     */
    private String imageType = "photo";

    private Integer count = 1;

    public ImageSearchAPITool() {
        setName("BingImageSearchAPI");
        setDescription("Enhance your apps and websites with image search options, from trends to detailed insights.");

        String serverUrl = BING_SERVER_URL;
        service = new BingService(serverUrl, Duration.ofSeconds(100L), true, token, null);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("ImageSearchAPITool toolInput:" + toolInput);
        Map<String, Object> response = service.imageSearch(toolInput, license, imageType, count);
        return new ToolExecuteResult(JSON.toJSONString(response));
    }
}
