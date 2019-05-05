
###### springboot mybatis 多数据配置

* [参考博客 springboot mybatis优雅的添加多数据源](https://www.cnblogs.com/kangoroo/p/7133543.html)


```
springboot的原则是简化配置，本文试图不通过xml配置，使用configuration配置数据源，并进行简单的数据访问。

并且配置了多数据源，在开发过程中这种场景很容易遇到。



1、依赖

springboot的starter

mybatis的springboot集成包

jdbc

复制代码
<dependencies>
　　<dependency>
    　　<groupId>org.springframework.boot</groupId>
    　　<artifactId>spring-boot-starter-web</artifactId>
　　</dependency>
　　<dependency>
    　　<groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
 　</dependency>
 　<dependency>
       <groupId>org.mybatis.spring.boot</groupId>
       <artifactId>mybatis-spring-boot-starter</artifactId>
       <version>1.1.1</version>
   </dependency>
</dependencies>
复制代码


2、在application中打开configuration

@Configuration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}


3、写主数据源的configuration

1)多数据源中有一个是主数据源，注意@primary注解的书写位置

2)MapperScan basePackages配置了扫描主数据源mapper的路径

3）//bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/sentinel/*.xml"));

注释掉了通过xml配置扩展sql的方式。如果频繁使用多表连接查询，可以打开自定义sql

复制代码
package com.dqa.sentinel.configuration;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.dqa.sentinel.mapper.sentinel", sqlSessionTemplateRef  = "sentinelSqlSessionTemplate")
public class SentinelDataSource {

    @Bean(name = "sentinelData")
    @ConfigurationProperties(prefix = "spring.datasource.sentinel") // application.properteis中对应属性的前缀
    @Primary
    public DataSource sentinelData() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sentinelSqlSessionFactory")
    @Primary
    public SqlSessionFactory sentinelSqlSessionFactory(@Qualifier("sentinelData") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/sentinel/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "sentinelTransactionManager")
    @Primary
    public DataSourceTransactionManager sentinelTransactionManager(@Qualifier("sentinelData") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sentinelSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sentinelSqlSessionTemplate(@Qualifier("sentinelSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
复制代码


4、比如你还有一个外部数据源，再写一个configuration

tips：这里不能有@primary注解，不然会有冲突

复制代码
package com.dqa.sentinel.configuration;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.dqa.sentinel.mapper.outer", sqlSessionTemplateRef  = "outerSqlSessionTemplate")
public class OuterDataSource {

    @Bean(name = "outerData")
    @ConfigurationProperties(prefix = "spring.datasource.outer") // application.properteis中对应属性的前缀
    public DataSource outData() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "outerSqlSessionFactory")
    public SqlSessionFactory outerSqlSessionFactory(@Qualifier("outerData") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/outer/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "outerTransactionManager")
    public DataSourceTransactionManager outerTransactionManager(@Qualifier("outerData") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "outerSqlSessionTemplate")
    public SqlSessionTemplate outerSqlSessionTemplate(@Qualifier("outerSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


}
复制代码


5、在mapper包中定义你需要的sql

路径要和刚才在configuration中配置的一样

复制代码
package com.dqa.sentinel.mapper.sentinel;

import com.dqa.sentinel.model.SentinelClan;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SentinelMapper {
    @Select("SELECT * FROM sentinelClan;")
    List<SentinelClan> getAllClan();

    @Select("SELECT * FROM sentinelClan WHERE id = #{id}")
    SentinelClan getOneClan(@Param("id") Integer id);

    @Insert("INSERT INTO sentinelClan (id,clanName,topicNames,bufferTime,countWidth,countPercent,alarmGroup,status,createTime,updateTime) " +
            "VALUES( #{id}, #{clanName}, #{topicNames}, #{bufferTime}, #{countWidth}, #{countPercent}, #{alarmGroup}, #{status}, #{createTime}, #{updateTime})")
    int insertOne(SentinelClan sentinelClan);

    @Update("UPDATE sentinelClan SET clanName = #{clanName},topicNames = #{topicNames},bufferTime = #{bufferTime}," +
            "countWidth = #{countWidth},countPercent = #{countPercent},alarmGroup = #{alarmGroup},status = #{status}," +
            "createTime=#{createTime}, updateTime=#{updateTime}" +
            "WHERE id = #{id}")
    int updateOne(SentinelClan sentinelClan);
}
复制代码


6、model中是实体类

个人爱好写的班班程程的，或者你可以在数据库建表之后使用generator自动生成。

复制代码
package com.dqa.sentinel.model;

import java.sql.Blob;
import java.util.Date;

public class SentinelClan {
    private Integer id ;
    private String clanName;
    private String topicNames;
    private Integer bufferTime;
    private Integer countWidth;
    private Integer countPercent;
    private String alarmGroup;
    private Integer status;
    private String createTime;
    private String updateTime;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getClanName() {
        return clanName;
    }
    public void setClanName(String clanName) {
        this.clanName = clanName;
    }
    public String getTopicNames() {
        return topicNames;
    }
    public void setTopicNames(String topicNames) {
        this.topicNames = topicNames;
    }
    public Integer getBufferTime() {
        return bufferTime;
    }
    public void setBufferTime(Integer bufferTime) {
        this.bufferTime = bufferTime;
    }
    public Integer getCountWidth() {
        return countWidth;
    }
    public void setCountWidth(Integer countWidth) {
        this.countWidth = countWidth;
    }
    public Integer getCountPercent() {
        return countPercent;
    }
    public void setCountPercent(Integer countPercent) {
        this.countPercent = countPercent;
    }
    public String getAlarmGroup() {
        return alarmGroup;
    }
    public void setAlarmGroup(String alarmGroup) {
        this.alarmGroup = alarmGroup;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public String getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}


```