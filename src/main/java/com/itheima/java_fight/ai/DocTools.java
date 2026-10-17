package com.itheima.java_fight.ai;

import com.itheima.java_fight.context.UserContext;
import com.itheima.java_fight.mapper.DocChunkMapper;
import com.itheima.java_fight.pojo.DocChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DocTools {

    private final DocChunkMapper docChunkMapper;

    public DocTools(DocChunkMapper docChunkMapper) {
        this.docChunkMapper = docChunkMapper;
    }

    @Tool(description = "列出当前登录用户上传过的文档名称（去重），返回文档名列表")
    public List<String> listMyDocs() {
        List<String> names = docChunkMapper.findByUserId(UserContext.getUserId())
                .stream()
                .map(DocChunk::getDocName)
                .distinct()
                .toList();
        log.info("listMyDocs invoked -> {}", names);
        return names;
    }
}