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
package com.alibaba.langengine.minimax.model;

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.minimax.model.model.MinimaxAudioStreamResult;
import com.alibaba.langengine.minimax.model.model.MinimaxText2SpeechParams;
import com.alibaba.langengine.minimax.model.service.MinimaxService;
import com.alibaba.langengine.minimax.model.model.MinimaxAudioStreamResult.StreamData;
import com.alibaba.langengine.minimax.model.model.MinimaxText2SpeechParams.TimberWeight;
import com.alibaba.langengine.minimax.model.utils.HttpStreamUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.alibaba.langengine.minimax.MiniMaxConfiguration.MINIMAX_API_TIMEOUT;

/**
 * @author aihe.ah
 * @time 2023/12/30
 * 功能说明：
 */
@Data
@Slf4j
public class MinimaxAudioTool extends DefaultTool {

    private MinimaxText2SpeechParams params;

    private String groupId;

    private String apiKey;

    /**
     * 写到那个文件中
     */
    private String fileName;

    /**
     * 是否流式返回语音内容
     */
    private Boolean stream;

    @JsonIgnore
    private MinimaxService service;

    /**
     * 消费音频文件内容
     */
    private Consumer<MinimaxAudioStreamResult> consumer;

    private  HexBinaryAdapter hexBinaryAdapter = new HexBinaryAdapter();

    public MinimaxAudioTool(String groupId, String apiKey) {
        this.apiKey = apiKey;
        this.groupId = groupId;
        this.service = new MinimaxService("https://api.minimax.chat",
            Duration.ofSeconds(Long.parseLong(MINIMAX_API_TIMEOUT)), true, apiKey);
    }

    private void init() {
        setName("Text2SpeechTool");
        setDescription(
            "This is a tool that can turn text into lifelike spoken audio. The input should be a text you want to "
                + "generate.");
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        params = prepareParams(toolInput);

        boolean shouldWriteToFile = StringUtils.isNotEmpty(fileName);

        try {
            if (stream != null && stream) {
                return handleStreaming(shouldWriteToFile);
            } else {
                return handleNonStreaming(shouldWriteToFile);
            }
        } catch (Exception e) {
            log.error("Error in processing", e);
            return new ToolExecuteResult(e.getMessage());
        }
    }

    private MinimaxText2SpeechParams prepareParams(String toolInput) {
        if (params == null) {
            params = createParams();
        }
        params.setText(toolInput);
        return params;
    }

    private ToolExecuteResult handleStreaming(boolean shouldWriteToFile) throws IOException {
        try (FileOutputStream fos = shouldWriteToFile ? new FileOutputStream(fileName) : null) {
            service.streamAudio(groupId, params)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> processStreamData(e, fos, shouldWriteToFile));
            return new ToolExecuteResult(fileName);
        }
    }

    private void processStreamData(MinimaxAudioStreamResult e, FileOutputStream fos, boolean shouldWriteToFile) {
        StreamData data = e.getData();
        if (data == null) {
            return;
        }

        if (data.getStatus() == 1) {
            byte[] audioBytes = hexBinaryAdapter.unmarshal(data.getAudio());
            data.setAudioBytes(audioBytes);
        }

        if (shouldWriteToFile) {
            writeAudioToFile(fos, data);
        }

        if (consumer != null) {
            consumer.accept(e);
        }
    }

    private void writeAudioToFile(FileOutputStream fos, StreamData data) {
        try {
            if (data.getAudioBytes() != null && data.getAudioBytes().length > 0) {
                fos.write(data.getAudioBytes());
            }
        } catch (IOException ex) {
            log.error("Error writing file", ex);
        }
    }

    private ToolExecuteResult handleNonStreaming(boolean shouldWriteToFile) throws IOException {
        ResponseBody responseBody = service.createAudio(groupId, params);
        okhttp3.MediaType mediaType = responseBody.contentType();

        if (mediaType != null && mediaType.toString().contains(MediaType.APPLICATION_JSON_VALUE)) {
            String response = HttpStreamUtils.convertStreamToString(responseBody.byteStream());
            log.info("Received response body: {}", response);
            return null;
        }

        if (shouldWriteToFile) {
            FileUtils.writeByteArrayToFile(new File(fileName), responseBody.bytes());
        }

        return new ToolExecuteResult(fileName);
    }

    private MinimaxText2SpeechParams createParams() {
        MinimaxText2SpeechParams request = new MinimaxText2SpeechParams();
        request.voiceId = "male-qn-qingse"; // 选择一个声音ID
        request.model = "speech-01";
        request.speed = 1.0;
        request.vol = 1.0;
        request.pitch = 0;

        // 创建 TimberWeight 对象列表
        List<TimberWeight> timberWeights = new ArrayList<>();
        MinimaxText2SpeechParams.TimberWeight timberWeight1 = new MinimaxText2SpeechParams.TimberWeight();
        timberWeight1.voiceId = "male-qn-qingse";
        timberWeight1.weight = 1;

        MinimaxText2SpeechParams.TimberWeight timberWeight2 = new MinimaxText2SpeechParams.TimberWeight();
        timberWeight2.voiceId = "female-shaonv";
        timberWeight2.weight = 1;

        // 添加 TimberWeight 对象到列表
        timberWeights.add(timberWeight1);
        timberWeights.add(timberWeight2);

        // 将 TimberWeight 列表设置到请求对象
        request.timberWeights = timberWeights;
        return request;
    }
}
