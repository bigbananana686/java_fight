package com.itheima.java_fight.service;


import com.itheima.java_fight.context.UserContext;
import com.itheima.java_fight.exception.BusinessException;
import com.itheima.java_fight.mapper.DocChunkMapper;
import com.itheima.java_fight.pojo.DocChunk;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocService {

    private static final int MAX_CHUNK_LEN = 1000;
    private final EmbeddingModel embeddingModel;
    private final DocChunkMapper docChunkMapper;
    private final ObjectMapper objectMapper;

    public DocService (EmbeddingModel embeddingModel,DocChunkMapper docChunkMapper,ObjectMapper objectMapper){
        this.embeddingModel = embeddingModel;
        this.docChunkMapper = docChunkMapper;
        this.objectMapper = objectMapper;
    }

    public int upload(MultipartFile file) {
        String text = readFile(file);
        List<String> pieces = split(text);
        Long userId = UserContext.getUserId();
        String docName = file.getOriginalFilename();

        List<DocChunk> chunks = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) {
            String piece = pieces.get(i);
            float[] vector = embeddingModel.embed(piece);
            chunks.add(new DocChunk(null, userId, docName, i, piece, toJson(vector), LocalDateTime.now()));
        }
        return docChunkMapper.insertBatch(chunks);
    }

    private String readFile(MultipartFile file){
        try{
            return new String(file.getBytes());
        }catch (IOException e){
            throw new BusinessException("文件读取失败");
        }
    }
    private String toJson(float[]vector){
        try{
            return objectMapper.writeValueAsString(vector);
        }catch(JacksonException e){
            throw new BusinessException("向量序列化失败");
        }
    }
    private List<String>split(String text){
        List<String>pieces = new ArrayList<>();
        String[]paragraphs = text.split("\\R\\s*\\R");
        for (String paragraph : paragraphs) {
            String p = paragraph.strip();
            if (p.isEmpty()) {
                continue;
            }
            for (int start = 0; start < p.length(); start += MAX_CHUNK_LEN) {
                pieces.add(p.substring(start, Math.min(p.length(), start + MAX_CHUNK_LEN)));
            }
        }
        return pieces;
    }

    private double dot(float[] a, float[] b) {
        double sum = 0;                       // 累加器
        for (int i = 0; i < a.length; i++) {  // 1024 个位置挨个走
            sum += a[i] * b[i];               // 对应位相乘，加进去
        }
        return sum;                           // 这个数就是相似度
    }

    private float[] fromJson(String json) {
        try {
            return objectMapper.readValue(json, float[].class);
        } catch (JacksonException e) {
            throw new BusinessException("向量反序列化失败");
        }
    }

    private static final int TOP_K = 3;

    // record：一次性装「分数 + 内容」的小类型
    private record Hit(double score, String content) {}

    public List<String> retrieve(String question) {
        float[] q = embeddingModel.embed(question);
        List<DocChunk> chunks = docChunkMapper.findByUserId(UserContext.getUserId());

        List<Hit> hits = new ArrayList<>();
        for (DocChunk c : chunks) {
            float[] v = fromJson(c.getEmbedding());
            hits.add(new Hit(dot(q, v), c.getContent()));
        }

        hits.sort((a, b) -> Double.compare(b.score, a.score));  // 分数高的排前面

        List<String> top = new ArrayList<>();
        for (int i = 0; i < Math.min(TOP_K, hits.size()); i++) {
            top.add(hits.get(i).content());
        }
        return top;
    }
}
