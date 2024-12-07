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
package com.alibaba.langengine.core.prompt.autogpt;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.BaseTool;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * autogpt prompt生成器
 *
 * @author xiaoxuan.lp
 */
@Data
public class PromptGenerator {

    public static final String FINISH_NAME = "finish";

    private List<String> constraints = new ArrayList<>();

    private List<BaseTool> commands = new ArrayList<>();

    private List<String> resources = new ArrayList<>();

    private List<String> performanceEvaluations = new ArrayList<>();

    /**
     * {
     * 	"thoughts": {
     * 		"text": "thought",
     * 		"reasoning": "reasoning",
     * 		"plan": "- short bulleted\n- list that conveys\n- long-term plan",
     * 		"criticism": "constructive self-criticism",
     * 		"speak": "thoughts summary to say to user"
     *        },
     * 	"command": {
     * 		"name": "command name",
     * 		"args": {
     * 			"arg name": "value"
     *        }
     *    }
     * }
     */
    private String responseFormat = "{\n" +
            "\t\"thoughts\": {\n" +
            "\t\t\"text\": \"thought\",\n" +
            "\t\t\"reasoning\": \"reasoning\",\n" +
            "\t\t\"plan\": \"- short bulleted\\n- list that conveys\\n- long-term plan\",\n" +
            "\t\t\"criticism\": \"constructive self-criticism\",\n" +
            "\t\t\"speak\": \"thoughts summary to say to user\"\n" +
            "\t},\n" +
            "\t\"command\": {\n" +
            "\t\t\"name\": \"command name\",\n" +
            "\t\t\"args\": {\n" +
            "\t\t\t\"arg name\": \"value\"\n" +
            "\t\t}\n" +
            "\t}\n" +
            "}";

    /**
     * Add a constraint to the constraints list.
     *
     * @param constraint
     * @return
     */
    public void addConstraint(String constraint) {
        constraints.add(constraint);
    }

    /**
     * Add a constraint to the constraints list.
     *
     * @param tool
     */
    public void addTool(BaseTool tool) {
        commands.add(tool);
    }

    /**
     * Add a resource to the resources list.
     *
     * @param resource
     */
    public void addResource(String resource) {
        resources.add(resource);
    }

    /**
     * Add a performance evaluation item to the performance_evaluation list.
     *
     * @param evaluation
     */
    public void addPerformanceEvaluation(String evaluation) {
        performanceEvaluations.add(evaluation);
    }

    public String generateCommandString(BaseTool tool) {
        String output = String.format("%s: %s", tool.getName(), tool.getDescription());
        output += String.format(", args json schema: %s", JSON.toJSONString(tool.getArgs()));
        return output;
    }

    public String generateNumberedList(List<String> items) {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            list.add(String.format("%d. %s", i + 1, item));
        }
        return list.stream().collect(Collectors.joining("\n"));
    }

    public String generateToolNumberedList() {
        List<String> commandStrings = new ArrayList<>();

        for(int i = 0; i < commands.size(); i++) {
            BaseTool command = commands.get(i);
            commandStrings.add(String.format("%d. %s", i + 1, generateCommandString(command)));
        }
        String finishDescription = "use this to signal that you have finished all your objectives";
        String finishArgs = "\"response\": \"final response to let people know you have finished your objectives\"";
        String finishString = String.format("%d. %s: %s, args: %s",
                commands.size() + 1, FINISH_NAME, finishDescription, finishArgs);
        commandStrings.add(finishString);

        return commandStrings.stream().collect(Collectors.joining("\n"));
    }

    /**
     * Generate a prompt string.
     *
     * @return
     */
    public String generatePromptString() {
        String formattedResponseFormat = responseFormat;

        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append(String.format("Constraints:\n%s\n\n", generateNumberedList(constraints)));
        promptBuilder.append("Commands:\n");
        promptBuilder.append(String.format("%s\n\n", generateToolNumberedList()));
        promptBuilder.append(String.format("Resources:\n%s\n\n", generateNumberedList(resources)));
        promptBuilder.append("Performance Evaluation:\n");
        promptBuilder.append(String.format("%s\n\n", generateNumberedList(performanceEvaluations)));
        promptBuilder.append("You should only respond in JSON format as described below ");
        promptBuilder.append(String.format("\nResponse Format: \n%s ", formattedResponseFormat));
        promptBuilder.append("\nEnsure the response can be parsed by Python json.loads");

        return promptBuilder.toString();
    }

    /**
     * This function generates a prompt string.
     * It includes various constraints, commands, resources, and performance evaluations.
     *
     * @return
     */
    public static String getPrompt(List<BaseTool> tools) {
        PromptGenerator promptGenerator = new PromptGenerator();

        promptGenerator.addConstraint("~4000 word limit for short term memory. Your short term memory is short, so immediately save important information to files.");
        promptGenerator.addConstraint("If you are unsure how you previously did something or want to recall past events, thinking about similar events will help you remember.");
        promptGenerator.addConstraint("No user assistance");
        promptGenerator.addConstraint("Exclusively use the commands listed in double quotes e.g. \"command name\"");

        // Add commands to the PromptGenerator object
        tools.stream().forEach(tool -> promptGenerator.addTool(tool));

        // Add resources to the PromptGenerator object
        promptGenerator.addResource("Internet access for searches and information gathering.");
        promptGenerator.addResource("Long Term memory management.");
        promptGenerator.addResource("GPT-3.5 powered Agents for delegation of simple tasks.");
        promptGenerator.addResource("File output.");

        // Add performance evaluations to the PromptGenerator object
        promptGenerator.addPerformanceEvaluation("Continuously review and analyze your actions to ensure you are performing to the best of your abilities.");
        promptGenerator.addPerformanceEvaluation("Constructively self-criticize your big-picture behavior constantly.");
        promptGenerator.addPerformanceEvaluation("Reflect on past decisions and strategies to refine your approach.");
        promptGenerator.addPerformanceEvaluation("Every command has a cost, so be smart and efficient. Aim to complete tasks in the least number of steps.");

        // Generate the prompt string
        String promptString = promptGenerator.generatePromptString();
        return promptString;
    }
}
