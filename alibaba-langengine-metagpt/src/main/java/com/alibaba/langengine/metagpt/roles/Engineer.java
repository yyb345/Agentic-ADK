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
package com.alibaba.langengine.metagpt.roles;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.metagpt.Message;
import com.alibaba.langengine.metagpt.actions.Action;
import com.alibaba.langengine.metagpt.actions.WriteCode;
import com.alibaba.langengine.metagpt.actions.WriteDesign;
import com.alibaba.langengine.metagpt.actions.WriteTasks;
import com.alibaba.langengine.metagpt.utils.CodeParser;
import com.alibaba.langengine.metagpt.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an Engineer role responsible for writing and possibly reviewing code.
 * 代表负责编写并可能审查代码的工程师角色。
 *
 * Attributes:
 *         name (str): Name of the engineer.
 *         profile (str): Role profile, default is 'Engineer'.
 *         goal (str): Goal of the engineer.
 *         constraints (str): Constraints for the engineer.
 *         nBorg (int): Number of borgs.
 *         useCodeReview (bool): Whether to use code review.
 *         todos (list): List of tasks.
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class Engineer extends Role {
    /**
     * 源码文件路径
     */
    public static final String SRC_CODE_FILEPATH = "SRC_CODE_FILEPATH";

    /**
     * List of tasks.
     */
    private List<String> todos = new ArrayList<>();

    /**
     * Number of borgs.
     */
    private int nBorg = 1;

    private static final String FILENAME_CODE_SEP = "#*001*#";

    private static final String MSG_SEP = "#*000*#";

    public Engineer() {
        this(null);

//        self.use_code_review = use_code_review
//        if self.use_code_review:
//        self.todos = []
//        self.n_borg = n_borg
    }

    public Engineer(BaseLanguageModel llm) {
        super("Alex",
                "Engineer",
                "Write elegant, readable, extensible, efficient code",
                "The code should conform to standards like PEP8 and be modular and maintainable", "", llm);

        List<Class<? extends Action>> actions = new ArrayList<>();
        actions.add(WriteCode.class);
        initActions(actions);

        List<Class<? extends Action>> watchActions = new ArrayList<>();
        watchActions.add(WriteTasks.class);
        watch(watchActions);

    }

    public Message actSp() {
        List codeMsgAll = new ArrayList<>();
        for (String todo : todos) {
            WriteCode writeCode = new WriteCode("", "", getLlm());
            String code = writeCode.run(getRc().getHistory().toString(), todo);
            File filePath = writeFile(todo, code);
            Message msg = new Message();
            msg.setContent(code);
            msg.setRole(getProfile());
            msg.setCauseBy(getRc().getTodo().getClass());
            getRc().getMemory().add(msg);
            String code_msg = todo + FILENAME_CODE_SEP + filePath.getPath();
            codeMsgAll.add(code_msg);
        }
        log.info("Done " + "workspace" + " generating.");
        String content = String.join(MSG_SEP, codeMsgAll);
        Message message = new Message();
        message.setContent(content);
        message.setRole(getProfile());
        message.setCauseBy(getRc().getTodo().getClass());
        message.setSendTo("QaEngineer");
        return message;
    }

    public File writeFile(String filename, String code) {
//        File workspace = get_workspace();
        filename = filename.replace("\"", "").replace("\n", "");
//        File file = new File(workspace, filename);
        String path = "/Users/xiaoxuan.lp/workspace/";
        File file = new File(path, filename);
        file.getParentFile().mkdirs();
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(code);
            writer.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return file;
    }

//    @Override
//    public void recv(Message message) {
//        getRc().getMemory().add(message);
//        if (getRc().getImportantMemory().contains(message)) {
//            todos = parseTasks(message);
//        }
//    }

//    @Override
//    public Message act() {
////        """Determines the mode of action based on whether code review is used."""
////        if self.use_code_review:
////            return await self._act_sp_precision()
//        return actSp();
//    }

    public static List<String> parseTasks(Message taskMsg) {
        if (taskMsg.getInstructContent() != null) {
            return (List<String>)taskMsg.getInstructContent().get("Task list");
        }
        return CodeParser.parseFileList("Task list", taskMsg.getContent(), "");
    }
    @Override
    protected List<Message> actMessages(){
        List<Message> mergeMemorys = new ArrayList<>();
        mergeMemorys.addAll(getRc().getImportantMemory());
        mergeMemorys.addAll(getRc().getMemory().getByAction(WriteDesign.class));
        return mergeMemorys;
    }

    @Override
    protected void afterAct(Message msg) {
        String jsonContent = msg.getContent();
        List<WriteCode.WriteCodeResult> writeCodeResultList = JSON.parseArray(jsonContent, WriteCode.WriteCodeResult.class);
        List<String> filePathList = new ArrayList<>();
        for (WriteCode.WriteCodeResult wrc : writeCodeResultList) {
            String filePath = FileUtils.writeFile(getRc().getEnv().getWorkspace() + "src/main/" + wrc.packageName, wrc.fileName, wrc.code);
            filePathList.add(filePath);
        }
        Map<String, Object> instructContent = new HashMap<>();
        instructContent.put(SRC_CODE_FILEPATH, filePathList);
        if (msg.getInstructContent() == null) {
            msg.setInstructContent(instructContent);
        } else {
            msg.getInstructContent().putAll(instructContent);
        }
    }
}
