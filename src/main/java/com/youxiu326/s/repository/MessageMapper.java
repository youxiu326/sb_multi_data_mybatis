package com.youxiu326.s.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface MessageMapper {

    @Insert({"insert into message(id,name,content) values (#{id},#{name},#{content})"})
    int insertMessage(@Param("id") Long id, @Param("name")String name, @Param("content")String content);

} 