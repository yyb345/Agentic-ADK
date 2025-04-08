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

import java.util.function.Consumer;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.minimax.model.model.MinimaxAudioStreamResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @author aihe.ah
 * @time 2023/12/30
 * 功能说明：
 */
public class MinimaxAudioToolTest {

    @Test
    public void testRun() throws Exception {
        // success
        String groupId = System.getenv("MINIMAX_GROUP_ID");
        String apiKey = System.getenv("MINIMAX_API_KEY");
        MinimaxAudioTool minimaxAudioTool = new MinimaxAudioTool(groupId, apiKey);
        minimaxAudioTool.setFileName("test.mp3");
        ToolExecuteResult result = minimaxAudioTool.run("你好啊， 我是小猪佩奇");
        System.out.println(result);
        Assertions.assertNotNull(result);
    }

    @Test
    public void testRunStream() throws Exception {
        // success
        String groupId = System.getenv("MINIMAX_GROUP_ID");
        String apiKey = System.getenv("MINIMAX_API_KEY");
        MinimaxAudioTool minimaxAudioTool = new MinimaxAudioTool(groupId, apiKey);
        minimaxAudioTool.setStream(true);
        minimaxAudioTool.setFileName("strem.mp3");
        minimaxAudioTool.setConsumer(new Consumer<MinimaxAudioStreamResult>() {
            @Override
            public void accept(MinimaxAudioStreamResult bytes) {
                // 音频的字符串，可以直接写入文件，或者输出到忘了中
                System.out.println(bytes.getData().getAudio());
            }
        });
        ToolExecuteResult result = minimaxAudioTool.run("你好啊，我是小猪佩奇");
        System.out.println(result);
        Assertions.assertNotNull(result);
    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme