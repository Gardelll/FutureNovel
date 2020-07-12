package net.wlgzs.futurenovel;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import javax.sql.DataSource;
import net.wlgzs.futurenovel.typehandler.UUIDTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.LongTypeHandler;
import org.hibernate.validator.HibernateValidator;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = AppConfig.class)
@MapperScan("net.wlgzs.futurenovel.dao")
public class AppConfig implements ApplicationContextAware, WebMvcConfigurer {
    private ApplicationContext applicationContext;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    // JSON
    @Bean
    public MappingJackson2HttpMessageConverter jsonMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }

    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        var ret = new RequestMappingHandlerAdapter();
        ret.setMessageConverters(List.of(jsonMessageConverter()));
        return ret;
    }

    // ResourceHandler
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
    }

    // Thymeleaf
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("/WEB-INF/thymeleaf/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(true);
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(){
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver viewResolver(){
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCharacterEncoding("UTF-8");
        return viewResolver;
    }

    // MyBatis
    @Bean
    public DataSource dataSource() {
        try {
            Properties properties = new Properties();
            properties.load(applicationContext.getResource("/WEB-INF/database_config.properties").getInputStream());
            var dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(properties.getProperty("jdbc_driverClassName"));
            dataSource.setUrl(properties.getProperty("jdbc_url"));
            dataSource.setUsername(properties.getProperty("jdbc_username"));
            dataSource.setPassword(properties.getProperty("jdbc_password"));
            return dataSource;
        } catch (IOException e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory() {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setTypeAliasesPackage("net.wlgzs.futurenovel.model");
        var conf = new org.apache.ibatis.session.Configuration();
        var reg = conf.getTypeHandlerRegistry();
        reg.setDefaultEnumTypeHandler(org.apache.ibatis.type.EnumOrdinalTypeHandler.class);
        reg.register(RoundingMode.class, org.apache.ibatis.type.EnumOrdinalTypeHandler.class);
        reg.register(UUID.class, UUIDTypeHandler.class);
        conf.addMappers("net.wlgzs.futurenovel.dao");
        factoryBean.setConfiguration(conf);
        return factoryBean;
    }

    // Email
    @Bean
    public MailSender mailSender() {
        try {
            var mailSender = new JavaMailSenderImpl();
            Properties properties = new Properties();
            properties.load(applicationContext.getResource("/WEB-INF/mail_config.properties").getInputStream());
            mailSender.setHost(properties.getProperty("email_host"));
            mailSender.setPort(Integer.parseInt(properties.getProperty("email_port")));
            mailSender.setUsername(properties.getProperty("email_username"));
            mailSender.setPassword(properties.getProperty("email_password"));
            mailSender.setJavaMailProperties(properties);
            return mailSender;
        } catch (IOException e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
    }

    // Validation
    @Override
    @Bean(name = "defaultValidator")
    public Validator getValidator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setProviderClass(HibernateValidator.class);
        return validatorFactoryBean;
    }

}
