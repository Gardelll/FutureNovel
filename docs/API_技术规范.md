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
|短时间内多次异常操作而被暂时禁止|403|HACK_DETECTED|检测到违规行为|
|使用没有管理权限的账号进行管理操作|403|ACCESS_DENIED|拒绝访问|
|注册或修改用户名时遇到冲突|400|USER_EXIST|用户名已存在|
|未完待续| | | |

### 数据格式

(剩下的待会写)
