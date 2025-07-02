package com.alibaba.langengine.docloader.yuque;

import java.net.URL;
import java.util.List;

import com.alibaba.langengine.core.docloader.BaseLoader;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.docloader.yuque.service.YuqueService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author aihe.ah
 * @time 2024/2/2
 * 功能说明：
 */
@Data
@Slf4j
public class YuqueUrlDocLoader extends BaseLoader {

    private YuqueService service;

    /**
     * 语雀的token
     */
    private String token;

    /**
     * 语雀的超时时间
     */
    private Long timeout;

    /**
     * 原始文档的地址，支持直接从文档加载内容；
     * 通过文档自动解析对应的namespace和slug
     */
    private String documentUrl;

    public YuqueUrlDocLoader(String token, Long timeout) {
        this.token = token;
        this.timeout = timeout;
    }

    @Override
    public List<Document> load() {
        // 解析语雀地址；
        // 通过文档自动解析对应的namespace和slug
        // 例如：https://www.yuque.com/aaaaaaaaaaaa/bbbbbbbbbbb/ccccccccc
        // aaaaaaaaaaaa:团队路径
        // bbbbbbbbbbb:知识库路径
        // ccccccccc:文档路径

        try {
            URL url = new URL(documentUrl);
            String path = url.getPath();

            // Remove leading and trailing slashes
            path = path.startsWith("/") ? path.substring(1) : path;
            path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

            // Split the path by slashes
            String[] parts = path.split("/");

            // Validate the URL path contains sufficient parts for namespace and slug
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid document url: " + documentUrl);
            }

            // Join the first two parts to form the namespace
            String namespace = parts[0] + "/" + parts[1];
            String slug = null;
            // Check if there's a slug, if more than two parts are present
            if (parts.length > 2 && StringUtils.isNotEmpty(parts[2])) {
                slug = parts[2];
            }

            log.info("namespace: {}, slug: {}", namespace, slug);
            YuqueDocLoader yuqueDocLoader = new YuqueDocLoader(token, timeout);
            yuqueDocLoader.setNamespace(namespace);
            yuqueDocLoader.setSlug(slug);
            return yuqueDocLoader.load();
        } catch (Exception e) {
            throw new RuntimeException("Error loading document from url: " + documentUrl, e);
        }

    }

}
