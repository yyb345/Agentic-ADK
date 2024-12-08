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
package com.alibaba.langengine.core.runnables;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.FunctionMessage;
import com.alibaba.langengine.core.prompt.PromptConverter;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.StructuredTool;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.openai.embeddings.OpenAIEmbeddings;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BaseTest {

    protected void chunkHandler(Object chunk) {
        if(chunk instanceof BaseMessage) {
            System.out.println(((BaseMessage) chunk).getContent());
        } else {
            System.out.println(JSON.toJSONString(chunk));
        }
    }

    protected VectorStore initVectorStore() {
        InMemoryDB vectorStore = new InMemoryDB();
        vectorStore.setEmbedding(new OpenAIEmbeddings());

        List<String> texts = Arrays.asList(new String[] {
                "harrison worked at kensho",
                "xiaoxuan worked at alibaba",
                "liping worked at taobao",
        });
        List<Document> documents =  texts.stream().map(text -> {
            Document document = new Document();
            document.setPageContent(text);
            return document;
        }).collect(Collectors.toList());
        vectorStore.addDocuments(documents);
        return vectorStore;
    }

    protected String convertXmlIntermediateSteps(List<AgentAction> intermediateSteps) {
        StringBuilder builder = new StringBuilder();
        for (AgentAction agentAction : intermediateSteps) {
            builder.append(String.format("<tool>%s</tool><tool_input>%s</tool_input>\n" +
                    "<observation>%s</observation>", agentAction.getTool(), agentAction.getToolInput(), agentAction.getObservation()));
        }
        return builder.toString();
    }

    protected BaseMessage convertMessageIntermediateSteps(List<AgentAction> intermediateSteps) {
        if(intermediateSteps.size() == 0) {
            return null;
        }
        FunctionMessage functionMessage = new FunctionMessage();
        for (AgentAction agentAction : intermediateSteps) {
            functionMessage.setName(agentAction.getTool());
            functionMessage.setContent(agentAction.getObservation());
        }
        return functionMessage;
    }

    protected String convertJsonIntermediateSteps(List<AgentAction> intermediateSteps) {
        String thoughts = "";
        for (AgentAction action : intermediateSteps) {
            thoughts += action.getLog();
            if(thoughts.endsWith("\n")) {
                thoughts = thoughts.substring(0, thoughts.length() - 1);
            }
            thoughts += "\nObservation: " + action.getObservation() + "\nThought:";
        }
        return thoughts;
    }

    protected String convertStepwisePlannerIntermediateSteps(List<AgentAction> intermediateSteps) {
        String thoughts = "";
        for (AgentAction action : intermediateSteps) {
            thoughts += action.getLog();
            if(thoughts.endsWith("\n")) {
                thoughts = thoughts.substring(0, thoughts.length() - 1);
            }
            thoughts += "\n\n[OBSERVATION]\n" + action.getObservation() + "\n";
        }
        return thoughts;
    }

    protected String convertTools(List<BaseTool> tools) {
        return tools.stream()
                .map(tool -> String.format("%s: %s", tool.getName(), tool.getDescription()))
                .collect(Collectors.joining("\n"));
    }

    protected String convertConversationalAgentTools(List<BaseTool> tools) {
        List<String> toolStringList = new ArrayList<>();
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                StructuredTool structuredTool = (StructuredTool) tool;
                String structSchema = structuredTool.formatStructSchema();
                toolStringList.add(String.format("%s: %s, args: %s", tool.getName(), tool.getDescription(), structSchema));
            } else {
                toolStringList.add(String.format("%s: %s", tool.getName(), tool.getDescription()));
            }
        }
        return toolStringList.stream().collect(Collectors.joining("\n"));
    }

    protected String convertStructuredChatAgentTools(List<BaseTool> tools) {
        String toolDesc = "{name_for_model}: {description_for_model}, args: {parameters}";
        List<String> toolStrings = new ArrayList<>();
        for (BaseTool tool : tools) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("name_for_model", tool.getName());
            inputs.put("name_for_human", tool.getHumanName());
            inputs.put("description_for_model", tool.getDescription());
            String structSchema;
            if(tool instanceof StructuredTool) {
                StructuredTool structuredTool = (StructuredTool) tool;
                structSchema = structuredTool.formatStructSchema();
            } else {
                structSchema = Pattern.compile("\\}").matcher(Pattern.compile("\\{").matcher(tool.getArgs().toString()).replaceAll("{{")).replaceAll("}}");
            }
            inputs.put("parameters", !StringUtils.isEmpty(structSchema) ? structSchema : "{}");
            String toolString = PromptConverter.replacePrompt(toolDesc, inputs);
            toolStrings.add(toolString);
        }
        return String.join("\n", toolStrings);
    }

    protected String convertQwenStructuredChatAgentTools(List<BaseTool> tools) {
        String toolDesc = "{name}: {description}";
        List<String> toolStrings = new ArrayList<>();
        for (BaseTool tool : tools) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("name", tool.getName());
            inputs.put("description", tool.getDescription());
            String toolString = PromptConverter.replacePrompt(toolDesc, inputs);
            toolStrings.add(toolString);
        }
        return String.join("\n", toolStrings);
    }

    protected String convertSemanticKernelAgentBasicPlannerTools(List<BaseTool> tools) {
        List<String> structSchemas = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                String skFunction = String.format("%s_%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);

                StructuredTool structuredTool = (StructuredTool)tool;
                String structSchema = structuredTool.formatSemantickernelBasicPrompt();
                structSchemas.add(structSchema);
            }
        }
        return String.join("\n\n", structSchemas);
    }

    protected String convertSemanticKernelAgentSequentialPlannerTools(List<BaseTool> tools) {
        List<String> structSchemas = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                String skFunction = String.format("%s_%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);

                StructuredTool structuredTool = (StructuredTool)tool;
                String structSchema = structuredTool.formatSemantickernelBasicPrompt("inputs");
                structSchemas.add(structSchema);
            }
        }
        return String.join("\n\n", structSchemas);
    }

    protected String convertSemanticKernelAgentStepwisePlannerTools(List<BaseTool> tools) {
        List<String> structSchemas = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                String skFunction = String.format("%s_%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);

                StructuredTool structuredTool = (StructuredTool)tool;
                String structSchema = structuredTool.formatSemantickernelStepwisePrompt();
                structSchemas.add(structSchema);
            }
        }
        return String.join("\n\n", structSchemas);
    }

    protected String convertSemanticKernelAgentActionPlannerTools(List<BaseTool> tools) {
        List<String> structSchemas = new ArrayList<>();
        Map<String, BaseTool> toolMap = new HashMap<>();
        for (BaseTool tool : tools) {
            if(tool instanceof StructuredTool) {
                String skFunction = String.format("%s_%s", tool.getName(), tool.getFunctionName());
                toolMap.put(skFunction, tool);

                StructuredTool structuredTool = (StructuredTool)tool;
                String structSchema = structuredTool.formatSemantickernelActionPrompt();
                structSchemas.add(structSchema);
            }
        }
        return String.join("\n\n", structSchemas);
    }

    protected String convertPlanAndExecuteTools(List<BaseTool> tools) {
        List<String> toolStrings = new ArrayList<>();
        for (BaseTool tool : tools) {
            String argsSchema = Pattern.compile("\\}").matcher(Pattern.compile("\\{").matcher(tool.getArgs().toString()).replaceAll("{{")).replaceAll("}}");
            toolStrings.add(String.format("%s: %s, args: %s", tool.getName(), tool.getDescription(), argsSchema));
        }
        return String.join("\n", toolStrings);
    }

    protected String convertToolNames(List<BaseTool> tools) {
        return String.join(", ", tools.stream().map(BaseTool::getName).toArray(String[]::new));
    }
}
