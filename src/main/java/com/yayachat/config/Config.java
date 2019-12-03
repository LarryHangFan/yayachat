package com.yayachat.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.yayachat.mapper",sqlSessionFactoryRef = "SqlSessionFactory")
public class Config {

    @Bean(name ="DataSource" )
    @Primary
    public DruidDataSource druidDataSource() {
        /* Druid 数据源配置 */
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/yayachatdb?characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true");//&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
        dataSource.setUsername("root");dataSource.setPassword("root");
       // dataSource.setPassword("123456");
        //初始连接数(默认值0)
        dataSource.setInitialSize(8);
        //最小连接数(默认值0)
        dataSource.setMinIdle(8);
        //最大连接数(默认值8,注意"maxIdle"这个属性已经弃用)
        dataSource.setMaxActive(32);
        return dataSource;
    }
    @Bean(name = "SqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("DataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:com/yayachat/mapper/*.xml"));
        sessionFactoryBean.setTypeAliasesPackage("com/yayachat/beans/*");
        return sessionFactoryBean.getObject();
    }
}
