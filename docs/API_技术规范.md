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
|使用没有管理权限的账号进行管理操作|403|ACCESS_DENIED|拒绝访问|
|注册或修改用户名、邮箱时遇到冲突|409|USER_EXIST|用户已存在|
|验证码错误|400|WRONG_ACTIVATE_CODE|验证码错误|
|未完待续| | | |

### 数据格式

+ UUID: 长度为 36 的随机且唯一字符串，例如 `11de527f-8b56-494e-9a02-0c11f8f31482` ，存储为 Cookie 时键名为 `uid`
+ Token: 长度为 64 的随机且唯一十六进制字符串，用于持久化用户的登录状态，存储为 Cookie 时键名为 `token`

-----------

### 登录与注册相关接口

#### 注册

1. 检查用户名或邮箱是否存在

```
GET /api/checkUsername
```

|字段|类型|含义或值|可空|
|---|---|------------|---|
|Content-Type|Header|application/x-www-form-urlencoded|否|
|name|GET 参数|用户名或邮箱|否
|type|GET 参数|字符枚举，username/email，默认为 username|是

若可以使用，服务端返回状态码 204 - No Content

2. 请求注册验证码

```
POST /api/sendCaptcha
```

|字段|类型|含义或值|可空|
|---|---|------------|---|
|Content-Type|Header|application/json|否|
|email|Json String|收件人|否|

若发送成功，服务端返回状态码 204 - No Content

3. 登录

```
POST /api/login
```

|字段|类型|含义或值|可空|
|---|---|------------|---|
|Content-Type|Header|application/json|否|
|User-Agent|Header|浏览器 UA，自动发送|否|
|userName|Json String|用户名或邮箱|否|
|password|Json String|密码|否
|redirectTo|Json String|登录完成后的跳转 Url|是

登录成功后设置 Cookie 并跳转到请求参数中 redirectTo 所指向的页面，
若请求参数不包含 redirectTo, 则使用 Session 变量中的值，或者跳转到首页。

即若登录成功，服务端返回状态码 302 - Found
