# FutureNovel

## 介绍
未来小说项目

## 项目结构说明
>- src 项目原代码  
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
git clone https://gitee.com/FutureNovel/FutureNovel.git
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

+ 导入数据结构

运行 SQL 脚本 `src/main/resources/database.sql`

+ 运行
```bash
mvn clean spring-boot:run
```
然后在浏览器访问 `http://localhost:8080/future-novel/` 即可

## 调试前端页面

输入命令
```bash
cd src/main/resources/templates
node server.js
```
可以在浏览器访问 `http://localhost:8848/` 查看原始模板


# ！！！此项目正处于开发阶段，无法使用！！！
