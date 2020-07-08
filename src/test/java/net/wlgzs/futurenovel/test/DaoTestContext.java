package net.wlgzs.futurenovel.test;

import net.wlgzs.futurenovel.typehandler.UUIDTypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Properties;

@Configuration
@MapperScan("net.wlgzs.futurenovel.dao")
public class DaoTestContext {
    @Bean
    DataSource dataSource() {
        try {
            Properties properties = new Properties();
            properties.load(DaoTestContext.class.getResourceAsStream("/database_config.properties"));
            var dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(properties.getProperty("jdbc_driverClassName"));
            dataSource.setUrl(properties.getProperty("jdbc_url"));
            dataSource.setUsername(properties.getProperty("jdbc_username"));
            dataSource.setPassword(properties.getProperty("jdbc_password"));
            return dataSource;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    SqlSessionFactoryBean sqlSessionFactory() {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setTypeAliasesPackage("net.wlgzs.futurenovel.model");
        var conf = new org.apache.ibatis.session.Configuration();
        conf.setDefaultEnumTypeHandler(org.apache.ibatis.type.EnumOrdinalTypeHandler.class);
        var reg = conf.getTypeHandlerRegistry();
        reg.register(RoundingMode.class, org.apache.ibatis.type.EnumOrdinalTypeHandler.class);
        reg.register(UUIDTypeHandler.class);
        conf.addMappers("net.wlgzs.futurenovel.dao");
        factoryBean.setConfiguration(conf);
        return factoryBean;
    }

    @Bean
    JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
}
