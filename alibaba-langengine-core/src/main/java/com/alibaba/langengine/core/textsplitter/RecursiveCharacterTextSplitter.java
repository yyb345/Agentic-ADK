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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of splitting text that looks at characters.
 * Recursively tries to split by different characters to find one that works.
 * 实现查看字符的分割文本。
 * 递归地尝试按不同的字符进行拆分以找到有效的字符。
 *
 * @author xiaoxuan.lp
 */
@Data
public class RecursiveCharacterTextSplitter extends TextSplitter {

    private List<String> separators = Arrays.asList(new String[] { "\n\n", "\n", " ", "" });

    private boolean isSeparatorRegex = false;

    private static final String EMPTY = "";

    @Override
    public List<String> splitText(String text) {
        return splitText(text, separators);
    }

    private List<String> splitText(String text, List<String> separators) {
        // 定义用于存储最终分割结果的列表
        List<String> finalChunks = new ArrayList<>();

        // 获取分隔符列表中的最后一个分隔符
        String separator = separators.get(separators.size() - 1);

        // 定义一个新的分隔符列表，用于存储还未使用的分隔符
        List<String> newSeparators = new ArrayList<>();

        // 遍历分隔符列表
        for (int i = 0; i < separators.size(); i++) {
            String s = separators.get(i);

            // 如果是正则表达式分隔符，则直接使用；否则使用Pattern.quote进行转义
            String _separator = isSeparatorRegex ? s :
                    Pattern.quote(s);

            // 如果找到空字符串分隔符，则将当前分隔符设置为空，并结束循环
            if (EMPTY.equals(s)) {
                separator = s;
                break;
            }

            // 检查当前分隔符是否能在文本中找到匹配
            if (Pattern.compile(_separator).matcher(text).find()) {
                // 设置当前分隔符，并更新待处理的分隔符列表，然后结束循环
                separator = s;
                newSeparators = separators.subList(i + 1, separators.size());
                break;
            }
        }
//        System.out.println("newSeparators:" + JSON.toJSONString(newSeparators));

        // 根据是否保留分隔符，设置分隔符
        String _separator = isSeparatorRegex ? separator :
                Pattern.quote(separator);
        // 使用选定的分隔符对文本进行分割
        List<String> splits = splitTextWithRegex(text, _separator, isKeepSeparator());
//        System.out.println("splits:" + JSON.toJSONString(splits));
        List goodSplits = new ArrayList<>();

        // 如果不保留分隔符，则将分隔符设置为空
        _separator = isKeepSeparator() ? "" : separator;

        // 遍历分割后的文本块
        for (String s : splits) {
//            System.out.println(getLength(s));
            // 如果文本块长度小于最大块大小，则添加到goodSplits列表
            if (getLength(s) < getMaxChunkSize()) {
                goodSplits.add(s);
            } else {
                // 如果goodSplits不为空，则将其合并并添加到最终结果列表
                if (!goodSplits.isEmpty()) {
                    List mergedText = mergeSplits(goodSplits, _separator);
                    finalChunks.addAll(mergedText);
                    goodSplits.clear();
                }
                // 如果没有更多的分隔符，则直接将当前块添加到最终结果列表
                if (newSeparators.isEmpty()) {
                    finalChunks.add(s);
                } else {
                    // 否则，使用新的分隔符列表对当前块进行进一步分割
                    List<String> otherInfo = splitText(s, newSeparators);
                    finalChunks.addAll(otherInfo);
                }
            }
        }

        // 如果goodSplits不为空，则将其合并并添加到最终结果列表
        if (!goodSplits.isEmpty()) {
            List mergedText = mergeSplits(goodSplits, _separator);
            finalChunks.addAll(mergedText);
        }

        // 返回最终的分割结果列表
        return finalChunks;
    }
}

