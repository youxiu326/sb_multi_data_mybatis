package com.youxiu326.p.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    @Insert({"insert into user(id,name,age) values (#{id},#{name},#{age})"})
    int insertUser(@Param("id") Long id, @Param("name")String name, @Param("age")Integer age);

}
