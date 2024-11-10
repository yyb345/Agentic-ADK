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
package com.alibaba.langengine.core.docloader;

import java.util.List;

import com.alibaba.langengine.core.docloader.partition.DocPartitioner;
import com.alibaba.langengine.core.docloader.partition.FilePartitioner;
import com.alibaba.langengine.core.docloader.partition.FileTypeEnum;
import com.alibaba.langengine.core.docloader.partition.PartitionContext;
import com.alibaba.langengine.core.docloader.partition.PdfPartitioner;
import com.alibaba.langengine.core.indexes.Document;

/**
 * @author aihe.ah
 * @time 2023/12/13
 * 功能说明：
 */
public class DocLoaderUtils {

    public static FilePartitioner getPartitioner(FileTypeEnum fileType) {
        switch (fileType) {
            case DOC:
                return new DocPartitioner();
            case PDF:
                return new PdfPartitioner();
            default:
                throw new IllegalArgumentException("Unsupported file type");
        }
    }

    /**
     * 基于类型进行partition
     * 入参为类型，和context
     */
    public static List<Document> partition(FileTypeEnum fileType, PartitionContext context) {
        FilePartitioner partitioner = getPartitioner(fileType);
        return partitioner.partition(context);
    }

}
