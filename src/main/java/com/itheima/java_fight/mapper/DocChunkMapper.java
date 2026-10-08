package com.itheima.java_fight.mapper;

import com.itheima.java_fight.pojo.DocChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocChunkMapper {

    int insertBatch(@Param("chunks") List<DocChunk> chunks);

    List<DocChunk> findByUserId(@Param("userId") Long userId);
}