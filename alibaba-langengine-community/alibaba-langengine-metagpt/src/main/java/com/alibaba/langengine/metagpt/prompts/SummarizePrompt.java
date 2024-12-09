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
package com.alibaba.langengine.metagpt.prompts;

public class SummarizePrompt {

    public static final String SUMMARIZE_PROMPT = "Your output should use the following template:\n" +
            "### Summary\n" +
            "### Facts\n" +
            "- [Emoji] Bulletpoint\n" +
            "\n" +
            "Your task is to summarize the text I give you in up to seven concise bullet points and start with a short, high-quality \n" +
            "summary. Pick a suitable emoji for every bullet point. Your response should be in {{SELECTED_LANGUAGE}}. If the provided\n" +
            " URL is functional and not a YouTube video, use the text from the {{URL}}. However, if the URL is not functional or is \n" +
            "a YouTube video, use the following text: {{CONTENT}}.";

    public static final String SUMMARIZE_PROMPT_2 = "Provide a very short summary, no more than three sentences, for the following article:\n" +
            "\n" +
            "Our quantum computers work by manipulating qubits in an orchestrated fashion that we call quantum algorithms.\n" +
            "The challenge is that qubits are so sensitive that even stray light can cause calculation errors — and the problem worsens as quantum computers grow.\n" +
            "This has significant consequences, since the best quantum algorithms that we know for running useful applications require the error rates of our qubits to be far lower than we have today.\n" +
            "To bridge this gap, we will need quantum error correction.\n" +
            "Quantum error correction protects information by encoding it across multiple physical qubits to form a “logical qubit,” and is believed to be the only way to produce a large-scale quantum computer with error rates low enough for useful calculations.\n" +
            "Instead of computing on the individual qubits themselves, we will then compute on logical qubits. By encoding larger numbers of physical qubits on our quantum processor into one logical qubit, we hope to reduce the error rates to enable useful quantum algorithms.\n" +
            "\n" +
            "Summary:\n";

    public static final String SUMMARIZE_PROMPT_3 = "Provide a TL;DR for the following article:\n" +
            "\n" +
            "Our quantum computers work by manipulating qubits in an orchestrated fashion that we call quantum algorithms. \n" +
            "The challenge is that qubits are so sensitive that even stray light can cause calculation errors — and the problem worsens as quantum computers grow. \n" +
            "This has significant consequences, since the best quantum algorithms that we know for running useful applications require the error rates of our qubits to be far lower than we have today. \n" +
            "To bridge this gap, we will need quantum error correction. \n" +
            "Quantum error correction protects information by encoding it across multiple physical qubits to form a “logical qubit,” and is believed to be the only way to produce a large-scale quantum computer with error rates low enough for useful calculations. \n" +
            "Instead of computing on the individual qubits themselves, we will then compute on logical qubits. By encoding larger numbers of physical qubits on our quantum processor into one logical qubit, we hope to reduce the error rates to enable useful quantum algorithms.\n" +
            "\n" +
            "TL;DR:";

    public static final String SUMMARIZE_PROMPT_4 = "Provide a very short summary in four bullet points for the following article:\n" +
            "\n" +
            "Our quantum computers work by manipulating qubits in an orchestrated fashion that we call quantum algorithms.\n" +
            "The challenge is that qubits are so sensitive that even stray light can cause calculation errors — and the problem worsens as quantum computers grow.\n" +
            "This has significant consequences, since the best quantum algorithms that we know for running useful applications require the error rates of our qubits to be far lower than we have today.\n" +
            "To bridge this gap, we will need quantum error correction.\n" +
            "Quantum error correction protects information by encoding it across multiple physical qubits to form a “logical qubit,” and is believed to be the only way to produce a large-scale quantum computer with error rates low enough for useful calculations.\n" +
            "Instead of computing on the individual qubits themselves, we will then compute on logical qubits. By encoding larger numbers of physical qubits on our quantum processor into one logical qubit, we hope to reduce the error rates to enable useful quantum algorithms.\n" +
            "\n" +
            "Bulletpoints:\n";

    public static final String SUMMARIZE_PROMPT_5 = "Please generate a summary of the following conversation and at the end summarize the to-do's for the support Agent:\n" +
            "\n" +
            "Customer: Hi, I'm Larry, and I received the wrong item.\n" +
            "\n" +
            "Support Agent: Hi, Larry. How would you like to see this resolved?\n" +
            "\n" +
            "Customer: That's alright. I want to return the item and get a refund, please.\n" +
            "\n" +
            "Support Agent: Of course. I can process the refund for you now. Can I have your order number, please?\n" +
            "\n" +
            "Customer: It's [ORDER NUMBER].\n" +
            "\n" +
            "Support Agent: Thank you. I've processed the refund, and you will receive your money back within 14 days.\n" +
            "\n" +
            "Customer: Thank you very much.\n" +
            "\n" +
            "Support Agent: You're welcome, Larry. Have a good day!\n" +
            "\n" +
            "Summary:";
}
