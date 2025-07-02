package com.alibaba.langengine.docloader.yuque.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 语雀文档信息
 *
 * @author xiaoxuan.lp
 */
@Data
public class YuqueDocInfo {

    private Long id;

    private String slug;

    private String title;

    private String description;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("book_id")
    private Long bookId;

    private String format;

    @JsonProperty("public")
    private Integer isPublic;

    private Integer status;

    @JsonProperty("view_status")
    private Integer viewStatus;

    @JsonProperty("read_status")
    private Integer readStatus;

    @JsonProperty("likes_count")
    private Integer likesCount;

    @JsonProperty("read_count")
    private Integer readCount;

    @JsonProperty("comments_count")
    private Integer commentsCount;

    @JsonProperty("content_updated_at")
    private String contentUpdatedAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("published_at")
    private String publishedAt;

    @JsonProperty("firstPublishedAt")
    private String first_published_at;

    @JsonProperty("draft_version")
    private Integer draftVersion;

    @JsonProperty("last_editor_id")
    private Long lastEditorId;

    @JsonProperty("word_count")
    private Integer wordCount;

    @JsonProperty("last_editor")
    private Map<String, Object> lastEditor;

    private Map<String, Object> creator;

    private Map<String, Object> book;

    private String body;

    @JsonProperty("body_html")
    private String bodyHtml;

    private Integer hits;
}
