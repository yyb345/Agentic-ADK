package com.alibaba.langengine.docloader.yuque;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.langengine.core.docloader.BaseLoader;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.docloader.yuque.service.YuqueDocInfo;
import com.alibaba.langengine.docloader.yuque.service.YuqueResult;
import com.alibaba.langengine.docloader.yuque.service.YuqueService;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 语雀文档加载器
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class YuqueDocLoader extends BaseLoader {

    private YuqueService service;

    /**
     * namespace代表是：团队路径 + 知识库路径；
     * 例如：lark/openapi
     */
    private String namespace;

    /**
     * slug一般是具体的文档路径
     * 例如：api
     */
    private String slug;

    private Integer offset = 0;

    /**
     * 官方接口限制最多一次只能批量100条
     */
    private Integer limit = 100;

    /**
     * 语雀API批请求最大限制
     */
    private static final int MAX_BATCH_SIZE = 100;

    /**
     * 语雀文档的域名，默认为https://aliyuque.antfin.com/
     */
    private String domain = "https://aliyuque.antfin.com/";

    private String optionalProperties;

    /**
     * 是否返回html内容
     */
    private boolean returnHtml = false;

    public YuqueDocLoader(String token, Long timeout) {
        service = new YuqueService(token, Duration.ofSeconds(timeout));
    }

    /**
     * 加载文档。
     * 根据配置加载单个文档或批量加载文档。
     *
     * @return 文档列表
     */
    @Override
    public List<Document> load() {
        return StringUtils.isEmpty(slug) ? loadBatchDocuments() : loadSingleDocument();
    }

    /**
     * 加载单个文档。
     * 当slug配置时，只加载特定的文档。
     *
     * @return 单个文档的列表
     */
    private List<Document> loadSingleDocument() {
        YuqueResult<YuqueDocInfo> detailResult = service.getDocDetail(namespace, slug);
        if (detailResult.getData() == null) {
            return new ArrayList<>();
        }

        Document document = createDocumentFromInfo(slug, detailResult.getData());
        return Lists.newArrayList(document);
    }

    /**
     * 批量加载文档。
     * 当slug未配置时，批量加载命名空间下的文档。
     *
     * @return 文档列表
     */
    private List<Document> loadBatchDocuments() {
        List<Document> documents = new ArrayList<>();
        YuqueResult<List<YuqueDocInfo>> batchResult;

        do {
            batchResult = service.getDocs(namespace, offset, MAX_BATCH_SIZE, optionalProperties);
            List<Document> batchDocuments = batchResult.getData().stream()
                .map(docInfo -> fetchDocumentDetail(docInfo.getSlug()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
            // 因为中间还有过滤空的逻辑，因此document的个数未必完全等于MAX_BATCH_SIZE
            documents.addAll(batchDocuments);
            offset += MAX_BATCH_SIZE;
        } while (offset < batchResult.getMeta().getTotal());

        return documents;
    }

    /**
     * 根据文档标识符获取文档的详细信息并创建文档对象。
     *
     * @param docSlug 文档标识符
     * @return 创建的文档对象或null（如果无法获取文档详情）
     */
    private Document fetchDocumentDetail(String docSlug) {
        YuqueResult<YuqueDocInfo> detailResult = service.getDocDetail(namespace, docSlug);
        return detailResult.getData() != null ? createDocumentFromInfo(docSlug, detailResult.getData()) : null;
    }

    /**
     * 根据YuqueDocInfo创建文档对象。
     *
     * @param docSlug 文档标识符
     * @param docInfo 文档信息
     * @return 创建的文档对象
     */
    private Document createDocumentFromInfo(String docSlug, YuqueDocInfo docInfo) {
        // 如果文档内容为空，则不创建文档对象
        if (StringUtils.isEmpty(docInfo.getBody())) {
            return null;
        }
        Document document = new Document();
        document.setUniqueId(docSlug);
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("url", getDomain() + getNamespace() + "/" + docSlug);
        metadata.put("title", docInfo.getTitle());
        metadata.put("contentUpdatedAt", docInfo.getContentUpdatedAt());
        metadata.put("updatedAt", docInfo.getUpdatedAt());
        metadata.put("createdAt", docInfo.getCreatedAt());
        metadata.put("publishedAt", docInfo.getPublishedAt());

        document.setMetadata(metadata);
        document.setPageContent(returnHtml ? docInfo.getBodyHtml() : docInfo.getBody());
        return document;
    }
}
