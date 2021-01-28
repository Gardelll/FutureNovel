# FutureNovel

## 介绍

[![Jenkins](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fgardel.top%2Fci%2Fjob%2Ffuture-novel%2F)](https://gardel.top/ci/job/future-novel/)
[![Uptime Robot ratio (30 days)](https://img.shields.io/uptimerobot/ratio/m786009349-5644004b825ccfbd2a7debce)](https://gardel.top/future-novel/)
[![State-of-the-art Shitcode](https://img.shields.io/static/v1?label=State-of-the-art&message=Shitcode&color=7B5804)](https://github.com/trekhleb/state-of-the-art-shitcode)
[![License](https://img.shields.io/badge/license-Apache%202-blue)](https://gitee.com/FutureStudio/FutureNovel/blob/master/LICENSE)


未来小说项目

[![图片](https://blog.gardel.top/wp-content/uploads/2020/10/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE-2020-10-06-165235.png)](https://gardel.top/future-novel/)

## 项目结构说明
>- src 项目源代码  
>  - main
>    - java  
>      - net.wlgzs.futurenovel  
>        - packet `Java DTO 类`  
>        - controller `Spring 控制器`  
>        - dao `数据库映射`  
>        - exception `应用异常`  
>        - filter `过滤器`  
>        - model `数据模型`  
>        - service `Spring 数据服务`  
>        - typehandler `Mybatis 数据类型映射器`  
>    - resources  
>      - static `前端相关资源文件`  
>      - templates `Thymeleaf 模板`  
>  - test  
>    - java `测试相关的 Java 代码`  
>    - resources `测试相关的资源与配置`  
>- docs `项目的文档`

## 编译与打包
+ 克隆这个项目  
```bash
git clone https://gitee.com/FutureStudio/FutureNovel.git
```

+ 修改配置文件 (没有的文件需要手动创建)
```
# 配置文件路径为：
# $HOME/future-novel/application.yml
# 若服务器 $HOME 路径不可写，可使用 JVM 参数 '-Duser.home=/path/to/writeable' 更改 $HOME 目录

server:
  port: 8080
  error:
    path: '/error'

logging:
  level:
    web: debug

spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: '8MB'
  thymeleaf:
    cache: false
  datasource:
    url: 'jdbc:mariadb://127.0.0.1:3306/novel_db?useSSL=false'
    username: 'novel_db'
    password: '数据库密码'
    driver-class-name: org.mariadb.jdbc.Driver
    type: com.mchange.v2.c3p0.ComboPooledDataSource
  mail:
    host: '发件邮箱服务器'
    username: '发件邮箱账号'
    password: '发件密码'
    port: 465
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable:
              true

mybatis:
  type-aliases-package: 'net.wlgzs.futurenovel.model'
  type-handlers-package: 'net.wlgzs.futurenovel.typehandler'
  configuration:
    default-enum-type-handler: org.apache.ibatis.type.EnumOrdinalTypeHandler

future: # 应用相关配置，详情见 `net.wlgzs.futurenovel.AppProperties`
  upload-dir: '/tmp' # 文件上传目录，默认为 $HOME/future-novel/uploads
  token:
    savePeriod: 10 # token 保存间隔，单位 分钟
    expire: 7 # token 过期时间，单位 天
    cookieExpire: 30 # token 浏览器 Cookie 过期时间，单位 天

```

+ 安装分词器（如果使用mariadb）

```shell
apt install mariadb-plugin-mroonga groonga-tokenizer-mecab
```

+ 导入数据结构

运行 SQL 脚本 `src/main/resources/database.sql`

+ 运行
```bash
mvn clean spring-boot:run
```
然后在浏览器访问 `http://localhost:8080/future-novel` 即可
