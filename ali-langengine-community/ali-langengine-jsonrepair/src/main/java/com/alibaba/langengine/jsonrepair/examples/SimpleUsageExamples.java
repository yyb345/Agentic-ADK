package com.alibaba.langengine.jsonrepair.examples;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.jsonrepair.JsonRepair;
import com.alibaba.langengine.jsonrepair.JsonSafeParser;

/**
 * SimpleUsageExamples - Simple examples of using JsonSafeParser and JsonRepair
 * 
 * This class provides simple, straightforward examples of how to use
 * JsonSafeParser and JsonRepair in your code.
 */
public class SimpleUsageExamples {
    
    /**
     * Main method to run all simple examples
     */
    public static void main(String[] args) {
        System.out.println("=== Running Simple Usage Examples ===\n");
        
        // Run all examples
        basicJsonSafeParserUsage();
        basicJsonRepairUsage();
        handlingLlmOutputs();
    }
    
    /**
     * Example 1: Basic JsonSafeParser usage
     * 
     * This example demonstrates the basic usage of JsonSafeParser.
     */
    public static void basicJsonSafeParserUsage() {
        System.out.println("=== Example 1: Basic JsonSafeParser usage ===");
        
        // Example 1: Parse a JSON object
        String jsonObjectStr = "{ \"name\": \"John\", \"age\": 30 }";
        JSONObject jsonObject = JsonSafeParser.parseObject(jsonObjectStr);
        System.out.println("Parsed JSON object: " + jsonObject);
        
        // Example 2: Parse a JSON array
        String jsonArrayStr = "[ {\"id\": 1}, {\"id\": 2} ]";
        JSONArray jsonArray = JsonSafeParser.parseArray(jsonArrayStr);
        System.out.println("Parsed JSON array: " + jsonArray);
        
        // Example 3: Get a valid JSON string
        String malformedJson = "{ \"name\": \"John\" \"age\": 30 }"; // Missing comma
        String validJson = JsonSafeParser.getValidJsonString(malformedJson);
        System.out.println("Valid JSON string: " + validJson);
        
        System.out.println("\n");
    }
    
    /**
     * Example 2: Basic JsonRepair usage
     * 
     * This example demonstrates the basic usage of JsonRepair.
     */
    public static void basicJsonRepairUsage() {
        System.out.println("=== Example 2: Basic JsonRepair usage ===");
        
        // Example 1: Repair a JSON string
        String malformedJson = "{ 'name': 'John', 'age': 30 }"; // Single quotes
        String repairedJson = JsonRepair.repairJson(malformedJson);
        System.out.println("Repaired JSON: " + repairedJson);
        
        // Example 2: Load and repair a JSON string
        String malformedJson2 = "{ name: \"John\", age: 30 }"; // Missing quotes around keys
        Object parsedObject = JsonRepair.loads(malformedJson2);
        System.out.println("Parsed object: " + parsedObject);
        
        // Example 3: Repair with skipJsonParse option
        String malformedJson3 = "{ \"items\": [1, 2, 3, ] }"; // Trailing comma
        Object parsedObject2 = JsonRepair.loads(malformedJson3, true);
        System.out.println("Parsed object (skip FastJSON): " + parsedObject2);
        
        System.out.println("\n");
    }
    
    /**
     * Example 3: Handling LLM outputs
     * 
     * This example demonstrates how to handle JSON from LLM outputs.
     */
    public static void handlingLlmOutputs() {
        System.out.println("=== Example 3: Handling LLM outputs ===");
        
        // Example LLM output with JSON
        String llmOutput = "Here's the user information you requested:\n\n" +
                          "```json\n" +
                          "{\n" +
                          "  \"user\": {\n" +
                          "    \"name\": \"John Doe\",\n" +
                          "    \"age\": 30,\n" +
                          "    \"email\": \"john@example.com\"\n" +
                          "  }\n" +
                          "}\n" +
                          "```\n\n" +
                          "Let me know if you need anything else!";
        
        // Extract JSON from the LLM output
        String extractedJson = extractJsonFromLlmOutput(llmOutput);
        System.out.println("Extracted JSON: " + extractedJson);
        
        // Parse the extracted JSON
        try {
            JSONObject jsonObject = JSON.parseObject(extractedJson);
            System.out.println("Successfully parsed with FastJSON");
            
            // Access user information
            if (jsonObject.containsKey("user")) {
                JSONObject user = jsonObject.getJSONObject("user");
                System.out.println("User name: " + user.getString("name"));
                System.out.println("User email: " + user.getString("email"));
            }
        } catch (JSONException e) {
            System.out.println("Failed to parse with FastJSON: " + e.getMessage());
            
            // Use JsonSafeParser as fallback
            JSONObject safeResult = JsonSafeParser.parseObject(extractedJson);
            System.out.println("JsonSafeParser result: " + safeResult);
        }
        
        // Example with malformed JSON in LLM output
        String malformedLlmOutput = "Here's the user information:\n\n" +
                                   "{\n" +
                                   "  \"user\": {\n" +
                                   "    \"name\": \"John Doe\",\n" +
                                   "    \"age\": 30,\n" +
                                   "    \"skills\": [\"Java\", \"Python\", \"JavaScript\",]\n" + // Trailing comma
                                   "  }\n" +
                                   "}\n\n" +
                                   "Let me know if you need anything else!";
        
        // Extract and repair JSON from the malformed LLM output
        String extractedMalformedJson = extractJsonFromLlmOutput(malformedLlmOutput);
        System.out.println("\nExtracted malformed JSON: " + extractedMalformedJson);
        
        // Use JsonSafeParser to handle the malformed JSON
        JSONObject safeResult = JsonSafeParser.parseObject(extractedMalformedJson);
        System.out.println("JsonSafeParser result: " + safeResult);
        
        // Access user information
        if (safeResult.containsKey("user")) {
            JSONObject user = safeResult.getJSONObject("user");
            System.out.println("User name: " + user.getString("name"));
            System.out.println("User skills: " + user.getJSONArray("skills"));
        }
        
        System.out.println("\n");
    }
    
    /**
     * Helper method to extract JSON from LLM output
     */
    private static String extractJsonFromLlmOutput(String llmOutput) {
        // Try to extract JSON from markdown code block
        int startIndex = llmOutput.indexOf("```json");
        if (startIndex != -1) {
            startIndex += 7; // Length of "```json"
            int endIndex = llmOutput.indexOf("```", startIndex);
            if (endIndex != -1) {
                return llmOutput.substring(startIndex, endIndex).trim();
            }
        }
        
        // If not in markdown, try to find JSON object
        startIndex = llmOutput.indexOf('{');
        if (startIndex != -1) {
            // Find matching closing brace
            int braceCount = 1;
            for (int i = startIndex + 1; i < llmOutput.length(); i++) {
                char c = llmOutput.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                
                if (braceCount == 0) {
                    return llmOutput.substring(startIndex, i + 1);
                }
            }
        }
        
        // Return original if no JSON found
        return llmOutput;
    }
}
