package com.yayachat.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.yayachat.mapper2",sqlSessionFactoryRef = "SqlSessionFactory2")
public class Config2 {
    @Bean(name ="DataSource2" )
    @Primary
    public DruidDataSource druidDataSource() {
        /* Druid 数据源配置 */
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/yayaappdb?characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true");//&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
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

//    @Primary
//    @Bean(name = "gsiotDataSource")
//    @ConfigurationProperties("spring.datasource.gsiot")
//    public DataSource masterDataSource(){
//        return DataSourceBuilder.create().build();
//    }

    @Bean(name = "SqlSessionFactory2")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("DataSource2") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:com/yayachat/mapper2/*.xml"));
        sessionFactoryBean.setTypeAliasesPackage("com/yayachat/beans/*");
        return sessionFactoryBean.getObject();
    }

}
