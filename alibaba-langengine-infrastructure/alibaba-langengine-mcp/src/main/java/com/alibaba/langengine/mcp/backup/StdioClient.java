///**
// * Copyright (C) 2024 AIDC-AI
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.langengine.mcp;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//
//public class StdioClient {
//
//    public static void main(String[] args) {
//        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        PrintWriter out = new PrintWriter(System.out, true);
//
//        try {
//            BufferedReader stdInput = new BufferedReader(new InputStreamReader(System.in));
//            String userInput;
//
//            while ((userInput = stdInput.readLine()) != null) {
//                out.println(userInput);
//                System.out.println("Client sent: " + userInput);
//                String serverResponse = in.readLine();
//                System.out.println("Server replied: " + serverResponse);
//            }
//        } catch (IOException e) {
//            System.err.println("Client error: " + e.getMessage());
//        }
//    }
//}
