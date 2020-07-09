# FutureNovel

## 介绍
未来小说项目

## 项目结构说明
>- src 项目原代码  
>  - main
>    - java  
>      - net.wlgzs.futurenovel  
>        - controller `Spring 控制器`  
>        - dao `数据库映射`  
>        - model `数据模型`
>        - service `Spring 数据服务`
>        - typehandler `Mybatis 数据类型映射器`
>    - resources `后端相关资源文件`  
>    - webapp
>      - resources `前端相关资源文件`
>      - WEB-INF `服务器配置文件`
>        - mybatis `MyBatis 设置`
>        - thymeleaf `Thymeleaf 模板`
>  - test  
>    - java `测试相关的 Java 代码`  
>    - resources `测试相关的资源与配置`  
>- docs `项目的文档`

## 编译与打包
+ 克隆这个项目  
```bash
git clone https://gitee.com/FutureNovel/FutureNovel.git
```

+ 修改数据库配置文件
```
# src/main/webapp/WEB-INF/database_config.properties
jdbc_driverClassName=org.mariadb.jdbc.Driver
jdbc_url=[jdbc链接]
jdbc_username=[数据库用户名]
jdbc_password=[密码]
```

+ 打包
```bash
cd FutureNovel
mvn clean package
```

+ 运行内置的 Tomcat
```bash
mvn cargo:run
```
然后在浏览器访问 `http://localhost:8080/future-novel/` 即可

## 调试前端页面

输入命令
```bash
cd src/main/webapp/WEB-INF/thymeleaf
node server.js
```
可以在浏览器访问 `http://localhost:8848/` 查看原始模板


# ！！！此项目正处于开发阶段，无法使用！！！