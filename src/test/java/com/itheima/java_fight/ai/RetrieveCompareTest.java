package com.itheima.java_fight.ai;

import com.itheima.java_fight.context.UserContext;
import com.itheima.java_fight.mapper.DocChunkMapper;
import com.itheima.java_fight.pojo.DocChunk;
import com.itheima.java_fight.service.DocService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Map;

@SpringBootTest
class RetrieveCompareTest {

    @Autowired private DocService docService;
    @Autowired private DocChunkMapper docChunkMapper;
    @Autowired private EmbeddingModel embeddingModel;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void compareManualDotVsVectorStore() {
        // ① 先问库：谁的 chunk 最多（避免猜 userId）
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT user_id, COUNT(*) AS c FROM tb_doc_chunk GROUP BY user_id ORDER BY c DESC");
        System.out.println("=== doc_chunk 分布: " + rows);
        if (rows.isEmpty()) {
            throw new IllegalStateException("tb_doc_chunk 是空的，先上传一份文档再跑");
        }
        Long userId = ((Number) rows.get(0).get("user_id")).longValue();
        List<DocChunk> chunks = docChunkMapper.findByUserId(userId);
        System.out.println("=== 用 userId=" + userId + "，chunk 数=" + chunks.size());

        String question = "大熊猫吃什么";

        // ② 手写路径：调生产代码 DocService.retrieve（内部 dot + TOP_K=3）
        UserContext.setUserId(userId);
        List<String> manual = docService.retrieve(question);
        System.out.println("=== 手写 dot top3 ===");
        for (int i = 0; i < manual.size(); i++) {
            System.out.println("  M" + (i + 1) + ": " + brief(manual.get(i)));
        }

        // ③ VectorStore 路径：同一批 chunk、同一条 query
        VectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        List<Document> docs = chunks.stream()
                .map(c -> new Document(c.getContent(), Map.of("user_id", String.valueOf(c.getUserId()))))
                .toList();
        store.add(docs);   // 逐条重新 embed，80 条约一分钟，慢是正常的

        List<Document> hits = store.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(3)
                .similarityThreshold(0.0)
                .build());
        System.out.println("=== VectorStore similaritySearch top3 ===");
        for (int i = 0; i < hits.size(); i++) {
            System.out.println("  V" + (i + 1) + ": " + brief(hits.get(i).getText())
                    + "  score=" + hits.get(i).getScore());
        }
        List<String> vectorTop = hits.stream().map(Document::getText).toList();
        assertEquals(manual, vectorTop, "手写 dot 与 VectorStore 的 top3 排序应当完全一致");
        UserContext.remove();
    }

    private String brief(String s) {
        String one = s.replaceAll("\\s+", " ");
        return one.length() > 40 ? one.substring(0, 40) + "…" : one;
    }
}