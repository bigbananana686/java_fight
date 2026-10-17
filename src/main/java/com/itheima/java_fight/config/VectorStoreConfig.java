package com.itheima.java_fight.config;

import com.itheima.java_fight.mapper.DocChunkMapper;
import com.itheima.java_fight.pojo.DocChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, DocChunkMapper docChunkMapper) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        List<DocChunk> chunks = docChunkMapper.findAll();
        if (!chunks.isEmpty()) {
            store.add(toDocuments(chunks));
        }
        log.info("vector store 启动装载完成: chunks={}", chunks.size());
        return store;
    }

    public static List<Document> toDocuments(List<DocChunk> chunks) {
        return chunks.stream()
                .map(c -> new Document(
                        "【块" + c.getId() + "】" + c.getContent(),
                        Map.of("user_id", String.valueOf(c.getUserId()),
                                "chunk_id", String.valueOf(c.getId()))))
                .toList();
    }
}