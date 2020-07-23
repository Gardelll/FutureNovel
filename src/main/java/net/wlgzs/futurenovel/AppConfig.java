package net.wlgzs.futurenovel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import javax.activation.FileTypeMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.typehandler.JsonNodeTypeHandler;
import net.wlgzs.futurenovel.typehandler.UUIDTypeHandler;
import org.hibernate.validator.HibernateValidator;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
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
@Slf4j
public class AppConfig implements ApplicationContextAware, WebMvcConfigurer {

    // Shared ObjectMapper
    public static final ObjectMapper objectMapper = new ObjectMapper();

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
        ret.getMessageConverters().add(jsonMessageConverter());
        var resourceMessageConverter = new ResourceHttpMessageConverter();
        resourceMessageConverter.setSupportedMediaTypes(List.of(MediaType.IMAGE_JPEG,
                MediaType.IMAGE_PNG,
                MediaType.IMAGE_GIF,
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.ALL));
        ret.getMessageConverters().add(resourceMessageConverter);
        return ret;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return objectMapper;
    }

    // ResourceHandler
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
//        registry.addResourceHandler("/swagger-ui/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
//                .resourceChain(false);
    }

    // File Upload
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
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
        // 缓存模板文件
        templateResolver.setCacheable(false);
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
        reg.register(JsonNode.class, JsonNodeTypeHandler.class);
        conf.addMappers("net.wlgzs.futurenovel.dao");
        factoryBean.setConfiguration(conf);
        return factoryBean;
    }

    // Email
    @Bean
    public JavaMailSender mailSender() {
        try {
            var mailSender = new JavaMailSenderImpl();
            Properties properties = new Properties();
            properties.load(applicationContext.getResource("/WEB-INF/mail_config.properties").getInputStream());
            mailSender.setHost(properties.getProperty("email_host"));
            mailSender.setPort(Integer.parseInt(properties.getProperty("email_port")));
            mailSender.setUsername(properties.getProperty("email_username"));
            mailSender.setPassword(properties.getProperty("email_password"));
            mailSender.setJavaMailProperties(properties);
            mailSender.setDefaultFileTypeMap(FileTypeMap.getDefaultFileTypeMap());
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

    // Application Configuration
    @Bean
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Properties futureNovelConfig() {
        Properties properties = new Properties();
        try (var input = applicationContext.getResource("/WEB-INF/future-novel_config.properties").getInputStream()) {
            properties.load(input);
        } catch (IOException ignored) {
            if (System.getProperty("future.uploadDir") != null)
                properties.setProperty("future.uploadDir", System.getProperty("future.uploadDir"));
        }
        if ("default".equalsIgnoreCase(properties.getProperty("future.uploadDir", "default"))) {
            properties.setProperty("future.uploadDir", System.getProperty("user.home", System.getenv("HOME")) + "/future-novel/uploads");
        }
        File uploadDir = new File(properties.getProperty("future.uploadDir"));
        if (!uploadDir.exists()) uploadDir.mkdirs();
        return properties;
    }

}
