package com.alibaba.langengine.jsonrepair;

import java.io.*;

/**
 * JsonFileUtils - Utility methods for handling JSON files
 * 
 * This class provides utility methods for reading from and writing to JSON files.
 */
public class JsonFileUtils {
    
    /**
     * Reads a JSON file and returns its contents as a string
     * 
     * @param filename The name of the file to read
     * @return The contents of the file as a string
     * @throws IOException If an I/O error occurs
     */
    public static String readJsonFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    /**
     * Writes a JSON string to a file
     * 
     * @param filename The name of the file to write to
     * @param jsonContent The JSON content to write
     * @throws IOException If an I/O error occurs
     */
    public static void writeJsonFile(String filename, String jsonContent) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(jsonContent);
        }
    }
    
    /**
     * Repairs a JSON file and writes the repaired JSON to a new file
     * 
     * @param inputFilename The name of the input file
     * @param outputFilename The name of the output file
     * @throws IOException If an I/O error occurs
     */
    public static void repairJsonFile(String inputFilename, String outputFilename) throws IOException {
        String jsonContent = readJsonFile(inputFilename);
        String repairedJson = JsonRepair.repairJson(jsonContent);
        writeJsonFile(outputFilename, repairedJson);
    }
    
    /**
     * Repairs a JSON file in place
     * 
     * @param filename The name of the file to repair
     * @throws IOException If an I/O error occurs
     */
    public static void repairJsonFileInPlace(String filename) throws IOException {
        repairJsonFile(filename, filename);
    }
    
    /**
     * Loads and repairs a JSON file, returning the parsed object
     * 
     * @param filename The name of the file to load and repair
     * @return The parsed object
     * @throws IOException If an I/O error occurs
     */
    public static Object loadJsonFile(String filename) throws IOException {
        return JsonRepair.fromFile(filename);
    }
}
