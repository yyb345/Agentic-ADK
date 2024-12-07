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
package com.alibaba.langengine.openai.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.util.FileTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.alibaba.langengine.openai.OpenAIConfiguration.OPENAI_API_KEY;
import static com.alibaba.langengine.openai.OpenAIConfiguration.OPENAI_SERVER_URL;

/**
 * openai 音译文本工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class AudioTranscriptionTool extends DefaultTool {

    private String token = OPENAI_API_KEY;

    private String baseUrl;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/";

    private void init() {
        setName("AudioTranscriptionsTool");
        //这是一个把audio翻译成文本内容的转换工具。输入应该是一个audio的URL链接。
        setDescription("This is a conversion tool that translates audio file into text content. The input should be a json string, which includes prompt, originalAudioUrl fields. OriginalAudioUrl is an audio URL link.");

        Map<String, Object> args = getArgs();
        Map<String, Object> promptMap = new TreeMap<>();
        promptMap.put("title", "prompt");
        promptMap.put("type", "string");
        args.put("prompt", promptMap);

        Map<String, Object> originalAudioUrlMap = new TreeMap<>();
        originalAudioUrlMap.put("title", "original audio url");
        originalAudioUrlMap.put("type", "string");
        args.put("originalAudioUrl", originalAudioUrlMap);
    }

    public AudioTranscriptionTool() {
        baseUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        init();
    }

    public AudioTranscriptionTool(String apiKey, Integer timeout) {
        baseUrl = !StringUtils.isEmpty(OPENAI_SERVER_URL) ? OPENAI_SERVER_URL : DEFAULT_BASE_URL;
        token = apiKey;
        init();
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("toolInput:" + toolInput);
        File audio = null;
        try {
            Map<String, String> toolInputMap = JSON.parseObject(toolInput, new TypeReference<TreeMap>(){});

            audio = FileTools.getFileFromUrl(toolInputMap.get("originalAudioUrl"));

            String url = "https://api.openai.com/v1/audio/transcriptions";
            Map<String, Object> params = new HashMap<>();
            params.put("model", "whisper-1");
            if(toolInputMap.containsKey("prompt")) {
                params.put("prompt", toolInputMap.get("prompt"));
            }
            String response = "";
//            String response = HttpUtils.doPostWithAudio(url, params, FileTools.getMultipartFile(audio), token);
            return new ToolExecuteResult(response, true);
        } catch (Throwable e) {
            log.error("openai AudioTranscriptionTool ask error", e);
            return new ToolExecuteResult("Answer: 当前系统有异常，请稍后再试." + System.currentTimeMillis(), true);
        } finally {
            if(audio != null) {
                audio.delete();
                audio.deleteOnExit();
            }
        }
    }
}
