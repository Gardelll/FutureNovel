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
|编辑小说时找不到对应的索引|404|NOVEL_NOT_FOUND|找不到小说|
|未完待续| | | |

### 数据格式

+ UUID: 长度为 36 的随机且唯一字符串，例如 `11de527f-8b56-494e-9a02-0c11f8f31482` ，存储为 Cookie 时键名为 `uid`
+ Token: 长度为 64 的随机且唯一十六进制字符串，用于持久化用户的登录状态，存储为 Cookie 时键名为 `token`

+ Account: 用户信息，序列化内容如下  

```json5
{
    "uid": "be0dcefb-52b2-4ed4-b161-51e45d7d50cb", // 用户 UID
    "userName": "用户名",
    "email": "邮箱",
    "phone": "手机号",
    "registerIP": "注册 IP",
    "lastLoginIP": null, // 上次登陆 IP，从未登陆为 null
    "registerDate": "2020年07月19日 14:59:28", // 注册时间
    "lastLoginDate": null, // 上次登陆时间，从未登陆为 null
    "status": "FINE", // 帐号状态，分为 正常 FINE, 未验证 UNVERIFIED, 封禁 BANED
    "permission": "USER", // 帐号所属权限组，分为 USER, AUTHOR, ADMIN
    "experience": 0, // 用户的积分
    "profileImgUrl": null, // 头像 URL 地址
    "level": 0, // 用户等级
    "vip": false, // 是否为 会员
    "uidNum": "be0dcefb52b24ed4b16151e45d7d50cb" // 用户 UID 的数字格式（十六进制）
}
```

+ NovelIndex：小说的目录，序列化内容如下
```json5
{
    "uniqueId": "94bcb21d-9e05-4509-b048-ffc582184d1f", // 小说 ID
    "uploader": "d46d71c7-b0cf-45c6-a082-0ff75281d959", // 上传者
    "copyright": "REPRINT",
    "title": "标题",
    "authors": "作者A,作者B", // 半角逗号分隔
    "description": "简介",
    "rating": 0,
    "tags": "测试", // 半角逗号分隔
    "series": "系列",
    "publisher": "出版社",
    "pubdate": "2020年07月22日 11:21:18",
    "coverImgUrl": null,
    "chapters": [ // 章节的 ID
      "b8e99ad0-d269-4d6f-bf1a-3733d9f6fa8e",
      "e8335b58-a522-449c-b3d8-320c0900b23b"
    ]
}
```


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

登录成功后设置 Cookie，
若请求参数不包含 redirectTo, 则使用 Session 变量中的值，或者跳转到首页。

若登录成功，服务端返回状态码 200 - OK

返回参数：  

```json5
{
  "redirectTo": "需要跳转的地址", 
  "account": {
    // 用户信息，见上方 #数据格式 Account
  }
}
```

>登陆成功后，浏览器会把 uid, token 保存到 Cookie，  
>以及每次操作使用 token 时，要保证使用相同的 User-Agent  
>下文接口如果如要登陆才能操作，会出现 `权限` 相关说明

#### 注销

```
GET $URL/api/logout
```

>权限：所有登陆用户

若注销成功，服务端返回状态码 204 - No Content

--------

### 用户管理相关接口

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
     // 用户信息，见上方 #数据格式 Account
  },
  {
     // 还可能有更多
  }
]
```

若该页没有数据，服务端返回状态码 204 - No Content

--------

### 小说管理相关接口

#### 创建小说

1. 创建目录信息

```
POST $URL/api/novel/addIndex
```

>权限：  
>&nbsp;普通用户：可浏览，发布转载、公版小说  
>&nbsp;认证作家：可以发布同人、原创小说  
>&nbsp;管理员：可进行所有操作

参数：

|字段|类型|含义或值|可空|
|---|---|------------|---|
|copyright|String|<a title="NO_COPYRIGHT 公版,REPRINT 转载,FAN_FICTION 同人,ORIGINAL 原创">版权</a>字符串枚举|F|
|title|String|标题|F|
|authors|String 数组|作者(JSON 数组)|F|
|description|String|简介|F|
|tags|String 数组|用于分类的标签(JSON 数组)|F|
|series|String|<a title="比如《三体》、《黑暗森林》、《死神永生》属于“地球往事三部曲”系列">系列</a>|T|
|publisher|String|出版社|F|
|pubdate|String|出版日期，格式："yyyy年MM月dd日 HH:mm:ss"|F|
|coverImgUrl|String|封面图像 URL|T|

示例：
```json5
{
  "copyright": "REPRINT",
  "title": "标题",
  "authors": ["作者A","作者B"],
  "description": "简介",
  "tags": ["测试"],
  "series": "系列",
  "publisher": "出版社",
  "pubdate": "2020年07月22日 11:21:18",
  "coverImgUrl": null
}
```

若创建成功，服务端返回状态码 201 - Created  
上传成功自动取得对该文章的编辑权限

返回参数：

```json5
{
  // 小说目录信息，见上方 #数据格式 NovelIndex
}
```

2. 添加章节

```
POST $URL/api/novel/{fromNovel}/addChapter
```

>权限：上传者、管理员

参数：

|字段|类型|含义或值|可空|
|---|---|------------|---|
|fromNovel|UUID|小说 ID，注意要包含在路径里，不是 JSON 参数|F|
|title|String|标题|T|

示例：  
`POST http://localhost:8080/future-novel/api/novel/94bcb21d-9e05-4509-b048-ffc582184d1f/addChapter`
```json5
{
  "title": "章标题"
}
```

若创建成功，服务端返回状态码 201 - Created  

返回参数：
```json5
{
  "uniqueId": "b8e99ad0-d269-4d6f-bf1a-3733d9f6fa8e", // 章节 ID
  "fromNovel": "94bcb21d-9e05-4509-b048-ffc582184d1f", // 小说 ID
  "title": "章标题",
  "sections": [] // 文章，待添加
}
```

3. 添加文章（小节）

```
POST $URL/api/novel/{fromNovel}/{fromChapter}/addSection
```

>权限：上传者、管理员

|字段|类型|含义或值|可空|
|---|---|------------|---|
|fromNovel|UUID|小说 ID，注意要包含在路径里，不是 JSON 参数|F|
|fromChapter|String|章节 ID 的前 8 位，也可以是全部 注意要包含在路径里，不是 JSON 参数|F|
|title|String|标题|T|
|text|String|文章内容（HTML）|F|

示例：  
`POST http://localhost:8080/future-novel/api/novel/94bcb21d-9e05-4509-b048-ffc582184d1f/b8e99ad0/addSection`
```json5
{
  "title": "第 1 节",  
  "text": "内容过长，不宜展示"
}
```

若创建成功，服务端返回状态码 201 - Created  

返回参数：
```json5
{
  "title": "第 1 节",
  "uniqueId": "43c8d320-047c-4af6-877b-bfe228841cd1" // 文章（小节） ID
}
```

4. 重新获取小说的目录信息

```
GET $URL/api/novel/{uniqueId}
```

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uniqueId|UUID|小说 ID，注意要包含在路径里，不是 query 参数|F|

若查询成功，服务端返回状态码 200 - OK

返回参数：
```json5
{
  // 小说目录信息，见上方 #数据格式 NovelIndex
}
```

5. 获取小说的某一章节信息

```
GET $URL/api/novel/chapter/{uniqueId}
```

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uniqueId|UUID|章节的 ID，注意要包含在路径里，不是 query 参数|F|

若查询成功，服务端返回状态码 200 - OK

返回参数：
```json5
{
  "uniqueId": "b8e99ad0-d269-4d6f-bf1a-3733d9f6fa8e",
  "fromNovel": "94bcb21d-9e05-4509-b048-ffc582184d1f",
  "title": "章标题",
  "sections": [
    "43c8d320-047c-4af6-877b-bfe228841cd1"
  ]
}
```

6. 获取小说的某一小节，包含文本


```
GET $URL/api/novel/section/{uniqueId}
```

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uniqueId|UUID|小节的 ID，注意要包含在路径里，不是 query 参数|F|

若查询成功，服务端返回状态码 200 - OK

返回参数：
```json5
{
  "uniqueId": "43c8d320-047c-4af6-877b-bfe228841cd1",
  "fromChapter": "b8e99ad0-d269-4d6f-bf1a-3733d9f6fa8e",
  "title": "第 1 节",
  "text": "内容过长，不宜展示"
}
```

7. 或者直接根据小说目录的 ID 获取所有章节的信息


```
GET $URL/api/novel/{uniqueId}/chapters
```

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uniqueId|UUID|小说 ID，注意要包含在路径里，不是 query 参数|F|

若查询成功，服务端返回状态码 200 - OK

返回参数：
```json5
[
  {
    "uniqueId": "b8e99ad0-d269-4d6f-bf1a-3733d9f6fa8e",
    "fromNovel": "94bcb21d-9e05-4509-b048-ffc582184d1f",
    "title": "章标题",
    "sections": [
      "43c8d320-047c-4af6-877b-bfe228841cd1"
    ]
  },
  {
    // 还可能有更多
  }
]
```
