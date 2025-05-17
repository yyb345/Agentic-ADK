package com.alibaba.langengine.jsonrepair.examples;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.jsonrepair.JsonRepair;
import com.alibaba.langengine.jsonrepair.JsonSafeParser;

/**
 * LlmJsonRepairExamples - Examples of repairing JSON from Large Language Models
 * 
 * This class provides examples specifically focused on common JSON errors
 * produced by Large Language Models (LLMs) and how to handle them.
 */
public class LlmJsonRepairExamples {
    
    /**
     * Main method to run all LLM-specific examples
     */
    public static void main(String[] args) {
        System.out.println("=== Running LLM JSON Repair Examples ===\n");
        
        // Run all examples
        exampleMarkdownWrappedJson();
        examplePartialJson();
        exampleJsonWithExplanations();
        exampleMultipleJsonObjects();
        exampleMixedFormats();
    }
    
    /**
     * Example 1: JSON wrapped in markdown code blocks
     * 
     * LLMs often return JSON wrapped in markdown code blocks with ```json and ``` delimiters.
     * This example shows how to handle such cases.
     */
    public static void exampleMarkdownWrappedJson() {
        System.out.println("=== Example 1: JSON wrapped in markdown code blocks ===");
        
        // JSON wrapped in markdown code blocks
        String llmOutput = "Here's the JSON data you requested:\n\n" +
                          "```json\n" +
                          "{\n" +
                          "  \"name\": \"John Doe\",\n" +
                          "  \"age\": 30,\n" +
                          "  \"skills\": [\"Java\", \"Python\", \"JavaScript\"]\n" +
                          "}\n" +
                          "```\n\n" +
                          "Let me know if you need anything else!";
        System.out.println("Original LLM output:\n" + llmOutput);

        // Extract JSON from markdown (simple approach)
        String extractedJson = extractJsonFromMarkdown(llmOutput);
        System.out.println("\nExtracted JSON: " + extractedJson);
        
        // Parse the extracted JSON
        try {
            JSONObject jsonObject = JSON.parseObject(extractedJson);
            System.out.println("Successfully parsed, name: " + jsonObject.getString("name"));
        } catch (JSONException e) {
            System.out.println("Failed to parse extracted JSON: " + e.getMessage());
            
            // Use JsonSafeParser as fallback
            JSONObject safeResult = JsonSafeParser.parseObject(extractedJson);
            System.out.println("JsonSafeParser result: " + safeResult);
        }
        
        System.out.println("\n");
    }
    
    /**
     * Example 2: Partial or incomplete JSON
     * 
     * LLMs sometimes generate partial or incomplete JSON, especially when hitting token limits.
     * This example shows how to handle such cases.
     */
    public static void examplePartialJson() {
        System.out.println("=== Example 2: Partial or incomplete JSON ===");
        
        // Partial JSON (cut off)
        String partialJson = "{\n" +
                            "  \"users\": [\n" +
                            "    {\n" +
                            "      \"id\": 1,\n" +
                            "      \"name\": \"John\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"id\": 2,\n" +
                            "      \"name\": \"Jane";
        System.out.println("Partial JSON (cut off):\n" + partialJson);

        // Use JsonSafeParser to handle the partial JSON
        JSONObject safeResult = JsonSafeParser.parseObject(partialJson);
        System.out.println("\nJsonSafeParser result: " + safeResult);
        
        // Get a valid JSON string
        String validJson = JsonSafeParser.getValidJsonString(partialJson);
        System.out.println("Valid JSON string: " + validJson);
        
        System.out.println("\n");
    }
    
    /**
     * Example 3: JSON with explanatory text inside
     * 
     * LLMs sometimes include explanatory text inside JSON, which breaks the format.
     * This example shows how to handle such cases.
     */
    public static void exampleJsonWithExplanations() {
        System.out.println("=== Example 3: JSON with explanatory text inside ===");
        
        // JSON with explanatory text
        String jsonWithExplanations = "{\n" +
                                     "  \"user\": {\n" +
                                     "    \"name\": \"John\", // This is the user's full name\n" +
                                     "    \"age\": 30, // Age in years\n" +
                                     "    /* The following section contains contact information */\n" +
                                     "    \"contact\": {\n" +
                                     "      \"email\": \"john@example.com\",\n" +
                                     "      \"phone\": \"123-456-7890\"\n" +
                                     "    }\n" +
                                     "  }\n" +
                                     "}";
        System.out.println("JSON with explanatory text:\n" + jsonWithExplanations);

        // Use JsonRepair to handle the JSON with comments
        String repairedJson = JsonRepair.repairJson(jsonWithExplanations);
        System.out.println("\nRepaired JSON: " + repairedJson);
        
        // Parse the repaired JSON
        try {
            JSONObject jsonObject = JSON.parseObject(repairedJson);
            System.out.println("Successfully parsed, user: " + jsonObject.getJSONObject("user"));
        } catch (JSONException e) {
            System.out.println("Failed to parse repaired JSON: " + e.getMessage());
        }
        
        System.out.println("\n");
    }
    
    /**
     * Example 4: Multiple JSON objects in one response
     * 
     * LLMs sometimes generate multiple JSON objects in one response.
     * This example shows how to handle such cases.
     */
    public static void exampleMultipleJsonObjects() {
        System.out.println("=== Example 4: Multiple JSON objects in one response ===");
        
        // Multiple JSON objects
        String multipleJson = "Here are two user profiles:\n\n" +
                             "User 1:\n" +
                             "{\"id\": 1, \"name\": \"John\", \"role\": \"admin\"}\n\n" +
                             "User 2:\n" +
                             "{\"id\": 2, \"name\": \"Jane\", \"role\": \"user\"}";
        System.out.println("Multiple JSON objects:\n" + multipleJson);

        // Extract and parse the first JSON object
        String firstJson = extractFirstJsonObject(multipleJson);
        System.out.println("\nExtracted first JSON: " + firstJson);
        
        JSONObject firstObject = JsonSafeParser.parseObject(firstJson);
        System.out.println("First object: " + firstObject);
        
        // Extract and parse the second JSON object
        String secondJson = extractSecondJsonObject(multipleJson);
        System.out.println("\nExtracted second JSON: " + secondJson);
        
        JSONObject secondObject = JsonSafeParser.parseObject(secondJson);
        System.out.println("Second object: " + secondObject);
        
        System.out.println("\n");
    }
    
    /**
     * Example 5: Mixed format JSON (inconsistent formatting)
     * 
     * LLMs sometimes generate JSON with inconsistent formatting.
     * This example shows how to handle such cases.
     */
    public static void exampleMixedFormats() {
        System.out.println("=== Example 5: Mixed format JSON (inconsistent formatting) ===");
        
        // Mixed format JSON
        String mixedFormatJson = "{\n" +
                                "  \"user\": {\n" +
                                "    name: 'John',\n" +
                                "    \"age\": 30,\n" +
                                "    'skills': [\"Java\", 'Python', JavaScript]\n" +
                                "  },\n" +
                                "  results: [\n" +
                                "    {id: 1, score: 95},\n" +
                                "    {id: 2, score: 88},\n" +
                                "  ]\n" +
                                "}";
        System.out.println("Mixed format JSON:\n" + mixedFormatJson);

        // Use JsonSafeParser to handle the mixed format JSON
        JSONObject safeResult = JsonSafeParser.parseObject(mixedFormatJson);
        System.out.println("\nJsonSafeParser result: " + safeResult);
        
        // Access nested properties to verify repair worked
        if (safeResult.containsKey("user")) {
            JSONObject user = safeResult.getJSONObject("user");
            System.out.println("User name: " + user.getString("name"));
            System.out.println("User skills: " + user.getJSONArray("skills"));
        }
        
        if (safeResult.containsKey("results")) {
            JSONArray results = safeResult.getJSONArray("results");
            System.out.println("Results: " + results);
        }
        
        System.out.println("\n");
    }
    
    /**
     * Helper method to extract JSON from markdown code blocks
     */
    private static String extractJsonFromMarkdown(String markdown) {
        // Simple extraction - find content between ```json and ```
        int startIndex = markdown.indexOf("```json");
        if (startIndex != -1) {
            startIndex += 7; // Length of "```json"
            int endIndex = markdown.indexOf("```", startIndex);
            if (endIndex != -1) {
                return markdown.substring(startIndex, endIndex).trim();
            }
        }
        
        // Fallback - try to find any JSON-like content
        startIndex = markdown.indexOf('{');
        if (startIndex != -1) {
            // Find matching closing brace
            int braceCount = 1;
            for (int i = startIndex + 1; i < markdown.length(); i++) {
                char c = markdown.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                
                if (braceCount == 0) {
                    return markdown.substring(startIndex, i + 1);
                }
            }
        }
        
        // Return original if no JSON found
        return markdown;
    }
    
    /**
     * Helper method to extract the first JSON object from text
     */
    private static String extractFirstJsonObject(String text) {
        int startIndex = text.indexOf('{');
        if (startIndex != -1) {
            // Find matching closing brace
            int braceCount = 1;
            for (int i = startIndex + 1; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                
                if (braceCount == 0) {
                    return text.substring(startIndex, i + 1);
                }
            }
        }
        return "{}"; // Return empty object if no JSON found
    }
    
    /**
     * Helper method to extract the second JSON object from text
     */
    private static String extractSecondJsonObject(String text) {
        // Find the first JSON object
        int firstStart = text.indexOf('{');
        if (firstStart != -1) {
            // Find matching closing brace
            int braceCount = 1;
            int firstEnd = -1;
            for (int i = firstStart + 1; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                
                if (braceCount == 0) {
                    firstEnd = i;
                    break;
                }
            }
            
            if (firstEnd != -1) {
                // Find the second JSON object
                int secondStart = text.indexOf('{', firstEnd + 1);
                if (secondStart != -1) {
                    // Find matching closing brace
                    braceCount = 1;
                    for (int i = secondStart + 1; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (c == '{') braceCount++;
                        else if (c == '}') braceCount--;
                        
                        if (braceCount == 0) {
                            return text.substring(secondStart, i + 1);
                        }
                    }
                }
            }
        }
        return "{}"; // Return empty object if no second JSON found
    }
}
