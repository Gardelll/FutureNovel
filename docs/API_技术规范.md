## 概述
本文档旨在为页面的一些 ajax 请求提供一份规范

## 基本约定
### 请求与响应格式
若无特殊说明，请求与响应均为 JSON 格式（如果有 body），`Content-Type` 均为 `application/json; charset=utf-8`。

### 响应错误信息格式
```json
{
	"error":"错误类型（机器可读）",
	"errorMessage":"错误的详细信息（人类可读）",
	"cause":"该错误的异常（可选，调试用）"
}
```
若未发生错误，则一般不包含上述字段

|异常情况|HTTP状态码|Error|Error Message|
|--------|----------|-----|------------|
|一般 HTTP 异常（非业务异常，如 _Not Found_、_Method Not Allowed_）|_未定义_|_该 HTTP 状态对应的 Reason Phrase（于 [HTTP/1.1](https://tools.ietf.org/html/rfc2616#section-6.1.1) 中定义）_|_未定义_|
|参数错误（包括请求体 json 解析失败）|400|ILLEGAL_ARGUMENT|参数格式错误（各异）|
|登陆状态失效或未登录|403|INVALID_TOKEN|未登录|
|用户名或密码错误|403|INVALID_PASSWORD|用户名或密码错误|
|短时间内多次异常操作而被暂时禁止|423|HACK_DETECTED|检测到违规行为|
|帐号异常|403|ACCESS_DENIED|拒绝访问|
|注册或修改用户名、邮箱时遇到冲突|409|USER_EXIST|用户已存在|
|验证码错误|400|WRONG_ACTIVATE_CODE|验证码错误|
|数据库异常|500|DATABASE_EXCEPTION|数据库异常|
|管理时所用帐号权限不足|403|PERMISSION_DENIED|无权操作|
|上传文件超过 8MB|413|FILE_TOO_LARGE|上传的文件过大|
|未完待续| | | |

### 数据格式

+ UUID: 长度为 36 的随机且唯一字符串，例如 `11de527f-8b56-494e-9a02-0c11f8f31482` ，存储为 Cookie 时键名为 `uid`
+ Token: 长度为 64 的随机且唯一十六进制字符串，用于持久化用户的登录状态，存储为 Cookie 时键名为 `token`

-----------

## 接口

假设 $URL 为应用跟路径，比如 `http://localhost:8080/future-novel`

### 登录与注册相关接口

#### 注册

1. 检查用户名或邮箱是否存在

```
GET $URL/api/checkUsername
```

>请求格式：`application/x-www-form-urlencoded`

|字段|类型|含义或值|可空|
|---|---|------------|---|
|name|查询参数|用户名或邮箱|否|
|type|查询参数|字符枚举，username/email，默认为 username|是|

若可以使用，服务端返回状态码 204 - No Content

2. 请求注册验证码

```
POST $URL/api/sendCaptcha
```

|字段|类型|含义或值|可空|
|---|---|------------|---|
|email|String|收件人|否|

若发送成功，服务端返回状态码 204 - No Content

#### 登录

```
POST $URL/api/login
```

|字段|类型|含义或值|可空|
|---|---|------------|---|
|userName|String|用户名或邮箱|否|
|password|String|密码|否|
|redirectTo|String|登录完成后的跳转 Url|是|

登录成功后设置 Cookie 并跳转到请求参数中 redirectTo 所指向的页面，
若请求参数不包含 redirectTo, 则使用 Session 变量中的值，或者跳转到首页。

即若登录成功，服务端返回状态码 302 - Found

>登陆成功后，浏览器会把 uid, token 保存到 Cookie，  
>以及每次操作使用 token 时，要保证使用相同的 User-Agent  
>下文接口如果如要登陆才能操作，会出现 `权限` 相关说明

#### 注销

```
GET $URL/api/logout
```

>权限：所有登陆用户

若注销成功，服务端返回状态码 204 - No Content

#### 修改帐号信息

```
GET $URL/api/account/edit
```

>权限：若为管理员，可修改所有帐号，否则只能修改自己的帐号

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uuid|UUID|需要修改的用户 UID，默认为自己|T|
|userName|String|用户名|T|
|password|String|密码|T|
|email|String|邮箱|T|
|phone|String|手机号|T|
|profileImgUrl|String|头像 URL|T|
|activateCode|String|邮箱验证码，当且仅当修改邮箱、密码时验证|T|

不包含的属性不会修改  
未发生任何改动则认为修改失败  
注意：修改邮箱时验证码的收件帐号为新邮箱  
注意：管理员修改自己的帐号时也需要验证邮箱

若修改成功，服务端返回状态码 204 - No Content

可能的错误代码：`USER_EXIST`, `ILLEGAL_ARGUMENT`, `INVALID_TOKEN`, `ACCESS_DENIED`, `WRONG_ACTIVATE_CODE`，`PERMISSION_DENIED`

#### 上传图片

```
PUT $URL/api/img/upload
```  

>请求格式：`multipart/form-data`  
>权限：所有登陆用户

|字段|类型|含义或值|可空|
|---|---|------------|---|
|file|File|图片文件|否|

若上传成功，返回状态码为 200 - OK

返回参数：

```json
{
  "url": "图片的 URL",
  "md5": "文件的 MD5 摘要",
  "mime": "image/jpeg"
}
```

#### 用户管理

1. 获取用户管理的总页数

```
GET $URL/admin/accounts/pages
```  

>请求格式：`application/x-www-form-urlencoded`  
>权限：管理员

|字段|类型|含义或值|可空|
|---|---|------------|---|
|per_page|查询参数 int|每页展示的帐号数量，默认为 20|是|

若查询成功，返回状态码为 200 - OK

返回参数：

```json5
{
  "per_page" : 20, // 每页展示的帐号数量 
  "pages": 1 //总共的页数
}
```

2. 获取所有的用户信息

```
GET $URL/admin/accounts/get
```  

>请求格式：`application/x-www-form-urlencoded`  
>权限：管理员

|字段|类型|含义或值|可空|
|---|---|------------|---|
|per_page|查询参数 int|每页展示的帐号数量，默认为 20|T|
|page|查询参数 int|页码|F|

若查询成功，返回状态码为 200 - OK

返回参数：  
```json5
[
  {
    "uid": "be0dcefb-52b2-4ed4-b161-51e45d7d50cb", // 用户 UID
    "userName": "用户名",
    "email": "邮箱",
    "phone": "手机号",
    "registerIP": "注册 IP",
    "lastLoginIP": null, // 上次登陆 IP，从未登陆为 null
    "registerDate": "2020年07月19日 14:59:28", // 注册时间
    "lastLoginDate": null, // 上次登陆时间，从未登陆为 null
    "status": "FINED", // 帐号状态，分为 正常 FINED, 未验证 UNVERIFIED, 封禁 BANED
    "permission": "USER", // 帐号所属权限组，分为 USER, AUTHOR, ADMIN
    "experience": 0, // 用户的积分
    "profileImgUrl": null, // 头像 URL 地址
    "level": 0, // 用户等级
    "vip": false, // 是否为 会员
    "uidNum": "be0dcefb52b24ed4b16151e45d7d50cb" // 用户 UID 的数字格式（十六进制）
  },
  {
     // 还可能有更多
  }
]
```

若该页没有数据，服务端返回状态码 204 - No Content

