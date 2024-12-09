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
 * Bing Spell Check工具
 * 参考：https://learn.microsoft.com/en-us/bing/search-apis/bing-spell-check/quickstarts/rest/python
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public class SpellCheckAPITool extends DefaultTool {

    private BingService service;

    private String token = BING_API_KEY;

    private String mkt = "en-US";

    /**
     * The type of spelling and grammar checks to perform. The following are the possible case-sensitive values:
     * proof — For document scenarios
     * Proof mode provides the most comprehensive checks, adding capitalization, basic punctuation, and other features to aid document creation, but it's available only in the en-US (English-United States), es-ES (Spanish-Spain), pt-BR (Portuguese-Brazil) markets. Note that Proof mode is a Beta in the Spanish and Portuguese markets. For all other markets, set the mode query parameter to Spell.
     *
     * NOTE: If the length of query text exceeds 4096 characters, it will be truncated to 4096 characters before being processed.
     * spell — For web search scenarios
     * Spell mode is more aggressive in order to return better search results. The Spell mode finds most spelling mistakes but doesn't find some of the grammar errors that Proof catches; for example, capitalization and repeated words.
     *
     * NOTE: The text string is not checked for spelling mistakes if the string exceeds the following maximum lengths based on language code.
     * 130 characters for en, de, es, fr, pl, pt, sv, ru, nl, nb, tr-tr, it, zh, and ko.
     * 65 characters for all others.
     * Also, Because square brackets ( '[' and ']' ) may cause inconsistent results, you should remove them before checking the string for spelling mistakes.
     */
    private String mode = "spell";

    public SpellCheckAPITool() {
        setName("BingSpellCheckAPI");
        setDescription("Help users correct spellings, separate brand, people names and slang while typing.");

        String serverUrl = BING_SERVER_URL;
        service = new BingService(serverUrl, Duration.ofSeconds(100L), true, token, null);
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.warn("SpellCheckAPITool toolInput:" + toolInput);
        Map<String, Object> response = service.spellCheck(toolInput, mkt, mode);
        return new ToolExecuteResult(JSON.toJSONString(response));
    }
}
