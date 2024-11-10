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
package com.alibaba.langengine.minimax.model.model;

import com.alibaba.fastjson.annotation.JSONField;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @author aihe.ah
 * @time 2023/12/30
 * 功能说明：
 */
@Data
public class MinimaxAudioStreamResult {
    @JSONField(name = "data")
    private StreamData data;

    @JSONField(name = "extra_info")
    private ExtraInfo extraInfo;

    @JSONField(name = "trace_id")
    private String traceId;

    @JSONField(name = "base_resp")
    private BaseResp baseResp;

    @Data
    public static class StreamData {
        @JSONField(name = "audio")
        private String audio;

        /**
         * 解码好的音频字节
         */
        @JsonIgnore
        private byte[] audioBytes;

        @JSONField(name = "status")
        private int status;

        @JSONField(name = "ced")
        private String ced;
    }

    @Data
    public static class ExtraInfo {
        @JSONField(name = "audio_length")
        private int audioLength;

        @JSONField(name = "audio_sample_rate")
        private int audioSampleRate;

        @JSONField(name = "audio_size")
        private int audioSize;

        @JSONField(name = "bitrate")
        private int bitrate;

        @JSONField(name = "word_count")
        private int wordCount;

        @JSONField(name = "invisible_character_ratio")
        private int invisibleCharacterRatio;
    }

    @Data
    public static class BaseResp {
        @JSONField(name = "status_code")
        private int statusCode;

        @JSONField(name = "status_msg")
        private String statusMsg;
    }
}
