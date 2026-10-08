package com.itheima.java_fight.pojo;

import java.time.LocalDateTime;

public class DocChunk {

    private Long id;
    private Long userId;
    private String docName;
    private Integer chunkIndex;
    private String content;
    private String embedding;
    private LocalDateTime createdAt;

    public DocChunk() {
    }

    public DocChunk(Long id, Long userId, String docName, Integer chunkIndex, String content, String embedding, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.docName = docName;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.embedding = embedding;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
