package com.youxiu326;

import com.youxiu326.p.repository.UserMapper;
import com.youxiu326.s.repository.MessageMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.PostMapping;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SbMultiDataMybatisApplicationTests {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void contextLoads() {

        int count1 = userMapper.insertUser(6L, "mybatis 主数据源", 25);

        int count2 = messageMapper.insertMessage(6L, "mybatis 从数据源", "这是内容");

        System.out.println("=========================");
        System.out.println(count1);
        System.out.println("=========================");
        System.out.println(count2);

    }

}
