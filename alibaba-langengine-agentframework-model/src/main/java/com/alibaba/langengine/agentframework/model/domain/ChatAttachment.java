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
package com.alibaba.langengine.agentframework.model.domain;

import lombok.Data;

import java.util.Map;

/**
 * 会话附件
 *
 * @author xiaoxuan.lp
 */
@Data
public class ChatAttachment {

    /**
     * 附件类型，图片
     */
    public static final String IMAGE_TYPE = "image";

    /**
     * 附件类型，文件
     */
    public static final String FILE_TYPE = "file";

    /**
     * 附件类型
     * image, file
     */
    private String type;

    /**
     * 属性
     * image: url,detail
     * {"url":"https://idealab-platform.oss-accelerate.aliyuncs.com/20231125/33808fdb-10ad-428c-8a76-11532ad93b15_idealab2.png?Expires=4102329600&OSSAccessKeyId=LTAI5tFJF3QLwHzEmkhLs9dB&Signature=YcbZrM98pHRRMd%2BGFaI2OiFf8Z8%3D",
     * "detail":"low"}
     *
     * file: url, file_name
     */
    private Map<String, Object> props;

    /** ------ 兼容前端逻辑 start ------ **/

    private String url;

    /** ------ 兼容前端逻辑 end ------ **/
}
