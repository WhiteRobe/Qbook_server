# 快购阅读-后台

[![License](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://github.com/WhiteRobe/Qbook_server/blob/master/LICENSE)

快购阅读是一个基于Vue和Node.js现代前端框架的SPA项目。此为其后台HTTP服务器。

## 项目初始化 Usage ##

**Step 1. 环境依赖**

Middleware:
-Tomcat 8.5;

Database:
-MySQL v8.0 兼容 MariaDB v5.5;

Backend：
-JDK v1.8;
-Servlet v3.1;

**Step 2. 配置全局参数**

1. 请将发布后的war包命名为`mywebapp`

2. 在`mywebapp/WEB-INF/web.xml`中修改：

字段名|示例值|说明
:-:|:-:|:-:
DRIVER_MYSQL|com.mysql.cj.jdbc.Driver|数据库的驱动
AdminName|root|登陆账户名
AdminPW|password|账户密码
URL|jdbc:mysql://127.0.0.1:3306/bookwebapp|数据库地址

**Step 3. 部署依赖**

将`mywebapp/lib/`中的jar依赖包发送到Tomcat目录下的`lib/`中。在JRE中同样进行该操作。

**Step 4. 全局默认管理员账号密码**

- 账号:`admin1355`
- 密码:`admin1355`

## 服务器接口 ##

参见 `文档/接口定义.doc`

## 数据库设计 ##

参见 `文档/数据字典.xls`
