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
package com.alibaba.langengine.core.textsplitter;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 中文分词器
 *
 * @author dapeng.fdp
 */
@Data
public class ChineseTextSplitter extends TextSplitter {

    private int chunkLen = 50;

    private int overLap = 10;

    @Override
    public List<String> splitText(String text) {
        return stringToList(text, chunkLen, overLap);
    }

    private List<String> stringToList(String str,int length,int overLap){
        List<String> list = new ArrayList<>();
        int size = str.length()/length+1;
        for (int i = 0; i < size; i++) {
            if (i==size-1){
                String substring = str.substring(i * length-overLap, str.length());
                list.add(substring);
            }else if(i==0){
                String substring = str.substring(i * length, (i + 1) * length);
                list.add(substring);
            }else{
                String substring = str.substring(i * length-overLap, (i + 1) * length);
                list.add(substring);
            }
        }
        return list;
    }
}