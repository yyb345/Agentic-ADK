package com.alibaba.langengine.jsonrepair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * JsonRepairCLI - Command-line interface for the JsonRepair utility
 * 
 * This class provides a command-line interface for repairing JSON strings.
 */
public class JsonRepairCLI {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            // No arguments, read from stdin
            try {
                String jsonStr = readFromStdin();
                Object result = JsonRepair.loads(jsonStr);
                System.out.println(JSON.toJSONString(result, SerializerFeature.PrettyFormat));
            } catch (IOException e) {
                System.err.println("Error reading from stdin: " + e.getMessage());
                System.exit(1);
            }
        } else if (args.length == 1) {
            // One argument, read from file
            String filename = args[0];
            try {
                Object result = JsonRepair.fromFile(filename);
                System.out.println(JSON.toJSONString(result, SerializerFeature.PrettyFormat));
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                System.exit(1);
            }
        } else if (args.length == 2 && args[0].equals("-o")) {
            // Output to file
            String filename = args[1];
            try {
                String jsonStr = readFromStdin();
                Object result = JsonRepair.loads(jsonStr);
                String repairedJson = JSON.toJSONString(result, SerializerFeature.PrettyFormat);
                JsonFileUtils.writeJsonFile(filename, repairedJson);
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }
        } else if (args.length == 3 && args[1].equals("-o")) {
            // Input and output files
            String inputFilename = args[0];
            String outputFilename = args[2];
            try {
                JsonFileUtils.repairJsonFile(inputFilename, outputFilename);
                System.out.println("Successfully repaired JSON and wrote to " + outputFilename);
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }
        } else {
            printUsage();
            System.exit(1);
        }
    }
    
    /**
     * Reads JSON from standard input
     * 
     * @return The JSON string read from stdin
     * @throws IOException If an I/O error occurs
     */
    private static String readFromStdin() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Prints usage information
     */
    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java JsonRepairCLI                   # Read from stdin, write to stdout");
        System.out.println("  java JsonRepairCLI input.json        # Read from file, write to stdout");
        System.out.println("  java JsonRepairCLI -o output.json    # Read from stdin, write to file");
        System.out.println("  java JsonRepairCLI input.json -o output.json  # Read from file, write to file");
    }
}
