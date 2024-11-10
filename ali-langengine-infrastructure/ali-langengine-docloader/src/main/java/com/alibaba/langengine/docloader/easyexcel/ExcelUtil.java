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
package com.alibaba.langengine.docloader.easyexcel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;

import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.Row;

/**
 * @author aihe.ah
 * @time 2023/10/12
 * 功能说明：
 */
public class ExcelUtil {
    /**
     * 根据给定的表头创建一个Excel文件。
     *
     * @param headers
     * @return
     */
    public static byte[] createExcelWithHeaders(Map<String, Object> headers, List<List<String>> dataList) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        List<List<String>> headerList = headers.keySet().stream()
            .map(Collections::singletonList)
            .collect(Collectors.toList());

        // 注意：此处的OutputStream不会被关闭
        EasyExcel.write(os)
            .head(headerList)
            .sheet("Sheet1")
            .registerWriteHandler(new SimpleColumnWidthStyleStrategy(200))
            .registerWriteHandler(new CommentWriteHandler(headers))
            .doWrite(dataList);

        return os.toByteArray();
    }

    public static void readExcelWithHeaders(InputStream is,
        Consumer<Map<String, Object>> processor) {
        if (is == null || processor == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        EasyExcel.read(is, new AnalysisEventListener<Map<Integer, String>>() {
            Map<Integer, String> headerMap = Maps.newHashMap();

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                // 将表头的映射存储起来
                headerMap = headMap;
            }

            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                Map<String, Object> rowMap = Maps.newHashMap();
                for (Map.Entry<Integer, String> headerEntry : headerMap.entrySet()) {
                    Integer columnIndex = headerEntry.getKey();
                    String key = headerEntry.getValue();
                    Object value = data.get(columnIndex);
                    rowMap.put(key, value);
                }
                processor.accept(rowMap);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).sheet().doRead();
    }

    /**
     * 使用EasyExcel将数据写入Excel。
     *
     * @param os      输出流，用于写入Excel数据。
     * @param headers 表头和对应的宽度。
     * @param data    数据列表，每个Map代表一行数据，key为表头，value为单元格的值。
     */
    public static void writeDataToExcel(OutputStream os, Map<String, Object> headers, List<Map<String, Object>> data) {
        if (os == null || headers == null || headers.isEmpty() || data == null || data.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }

        List<List<String>> headerList = headers.keySet().stream()
            .map(Collections::singletonList)
            .collect(Collectors.toList());
        // 创建Sheet并设置表头宽度
        WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1")
            .head(headerList)
            .registerWriteHandler(new SimpleColumnWidthStyleStrategy(200))
            .build();
        // 创建Excel写入器
        ExcelWriter excelWriter = EasyExcel.write(os).build();
        // 写入的数据
        List<List<Object>> dataList = data.stream()
            .map(rowMap -> headers.keySet().stream()
                .map(rowMap::get)
                .collect(Collectors.toList()))
            .collect(Collectors.toList());
        excelWriter.write(dataList, writeSheet);

        // 关闭Excel写入器
        excelWriter.finish();
    }

    public static class CommentWriteHandler implements RowWriteHandler {
        private Map<String, Object> comments;

        public CommentWriteHandler(Map<String, Object> comments) {
            this.comments = comments;
        }

        @Override
        public void afterRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
            Integer relativeRowIndex, Boolean isHead) {
            if (isHead && relativeRowIndex == 1) {
                // 在第二行（索引为1）添加注释
                int columnIndex = 0;
                for (Map.Entry<String, Object> entry : comments.entrySet()) {
                    row.createCell(columnIndex).setCellValue(String.valueOf(entry.getValue()));
                    columnIndex++;
                }
            }
        }

    }

}
