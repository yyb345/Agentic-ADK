package com.alibaba.langengine.core.dflow.agent;

import java.util.Arrays;
import java.util.List;

import com.alibaba.langengine.core.dflow.agent.tool.Terminate;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.tool.BaseTool;

public class DFlowManusAgent extends DFlowToolCallAgent {

    private static final String SYSTEM_PROMPT = "You are OpenManus, an all-capable AI assistant, aimed at solving any task presented by the user. You have various tools at your disposal that you can call upon to efficiently complete complex requests. Whether it's programming, information retrieval, file processing, or web browsing, you can handle it all.";
    private static final String NEXT_STEP_PROMPT = "You can interact with the computer using PythonExecute, save important content and information files through FileSaver, open browsers with BrowserUseTool, and retrieve information using GoogleSearch.\n"
        + "\n"
        + "PythonExecute: Execute Python code to interact with the computer system, data processing, automation tasks, etc.\n"
        + "\n"
        + "FileSaver: Save files locally, such as txt, py, html, etc.\n"
        + "\n"
        + "BrowserUseTool: Open, browse, and use web browsers.If you open a local HTML file, you must provide the absolute path to the file.\n"
        + "\n"
        + "GoogleSearch: Perform web information retrieval\n"
        + "\n"
        + "Terminate: End the current interaction when the task is complete or when you need additional information from the user. Use this tool to signal that you've finished addressing the user's request or need clarification before proceeding further.\n"
        + "\n"
        + "Based on user needs, proactively select the most appropriate tool or combination of tools. For complex tasks, you can break down the problem and use different tools step by step to solve it. After using each tool, clearly explain the execution results and suggest the next steps.\n"
        + "\n"
        + "Always maintain a helpful, informative tone throughout the interaction. If you encounter any limitations or need more details, clearly communicate this to the user before terminating.\n";

    private static String name = "Manus";
    private String description = "A versatile agent that can solve various tasks using multiple tools";

    public DFlowManusAgent(BaseChatMemory memory, List<BaseTool> tools, BaseLLM llm) {
        super(name,memory, tools,llm);

        setSystemPrompt(SYSTEM_PROMPT);
        setNextStepPrompt(NEXT_STEP_PROMPT);

    }

    public DFlowManusAgent(BaseChatMemory memory,BaseLLM llm) {
       this(memory,Arrays.asList(
            //new PythonExecute(),
            //new GoogleSearch(),
            //            new BrowserUseTool(),
            //new FileSaver(),
            new Terminate()
        ),llm);
    }
}