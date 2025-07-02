package com.alibaba.langengine.modelcontextprotocol.examples;

import com.alibaba.langengine.modelcontextprotocol.client.transport.ServerParameters;
import com.alibaba.langengine.modelcontextprotocol.spec.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client implementation for the filesystem server.
 */
public class FilesystemClient extends BaseMcpClient {

	@Override
	protected ServerParameters createServerParameters() {
		return ServerParameters.builder("node")
//			.arg("-y")
//			.arg("@modelcontextprotocol/server-filesystem")
//			.arg("/Users/aihe/Desktop")
//			.arg("/Users/aihe/Downloads")
			.arg("/Users/aihe/Desktop/mcp-frontend/server.js")
			.build();
	}

	@Override
	protected void runTest(List<Tool> tools) {
		System.out.println("Testing filesystem server...");

		// 尝试找到文件系统相关工具
		Tool listTool = findToolByNamePrefix(tools, "list");

		if (listTool != null) {
			// 列出根目录
			System.out.println("\nListing root directories:");
			Map<String, Object> params = new HashMap<>();
			params.put("path", "/Users/aihe/Desktop");
			callToolAndPrintResult(listTool, params);
		}

		// 尝试获取文件工具
		Tool readTool = findToolByNamePrefix(tools, "read");
		if (readTool != null) {
			// 读取一个文件示例
			System.out.println("\nReading a file (if available):");
			Map<String, Object> params = new HashMap<>();
			params.put("path",
					"/Users/aihe/Desktop/_aidc_agent_process_define_process_definition_content_1742818214259.txt"); // 尝试读取一个可能存在的文件
			callToolAndPrintResult(readTool, params);
		}
	}

	public static void main(String[] args) {
		FilesystemClient client = new FilesystemClient();
		client.initialize("Filesystem Client");
		client.run();
	}

}
