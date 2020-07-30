<!-- TOC -->

- [概述](#概述)
- [基本约定](#基本约定)
    - [请求与响应格式](#请求与响应格式)
    - [响应错误信息格式](#响应错误信息格式)
    - [数据格式](#数据格式)
- [接口](#接口)
    - [登录与注册相关接口](#登录与注册相关接口)
        - [注册](#注册)
        - [登录](#登录)
        - [注销](#注销)
    - [用户管理相关接口](#用户管理相关接口)
        - [修改帐号信息](#修改帐号信息)
        - [上传图片](#上传图片)
        - [根据用户 ID 查询用户信息](#根据用户-id-查询用户信息)
        - [用户管理](#用户管理)
    - [小说管理相关接口](#小说管理相关接口)
        - [创建小说](#创建小说)
        - [编辑小说 - 获取原数据](#编辑小说---获取原数据)
        - [编辑小说](#编辑小说)
        - [删除小说](#删除小说)
        - [搜索小说](#搜索小说)

<!-- /TOC -->
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
|一般 HTTP 异常（非业务异常，如 _Not Found_、_Method Not Allowed_）|_未定义_|_该 HTTP 状态对应的 Reason Phrase（于 [HTTP/1.1](https://tools.ietf.org/html/rfc7231#section-6) 中定义）_|_未定义_|
|参数错误（包括请求体 json 解析失败）|400|ILLEGAL_ARGUMENT|参数格式错误（各异）|
|登陆状态失效或未登录|403|INVALID_TOKEN|未登录|
|用户名或密码错误|403|INVALID_PASSWORD|用户名或密码错误|
|短时间内多次异常操作而被暂时禁止|423|HACK_DETECTED|检测到违规行为|
|帐号异常|403|ACCESS_DENIED|拒绝访问|
|注册或修改用户名、邮箱时遇到冲突|409|USER_EXIST|用户已存在|
|账号由管理员创建但未验证邮箱|403|USER_UNVERIFIED|账号未激活|
|验证码错误|400|WRONG_ACTIVATE_CODE|验证码错误|
|数据库异常|500|DATABASE_EXCEPTION|数据库异常|
|管理时所用帐号权限不足|403|PERMISSION_DENIED|无权操作|
|上传文件超过 8MB|413|FILE_TOO_LARGE|上传的文件过大|
|编辑小说时找不到对应的索引|404|NOVEL_NOT_FOUND|找不到小说|
|执行需要积分的操作时积分不够|403|EXP_NOT_ENOUGH|积分不足|
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
    "authors": [
        "作者A",
        "作者B"
    ],
    "description": "简介",
    "rating": 0,
    "tags": [
        "测试"
    ],
    "series": "系列",
    "publisher": "出版社",
    "pubdate": "2020年07月22日 11:21:18",
    "coverImgUrl": null,
    "chapters": [
        {
            "uniqueId": "b8e99ad0-d269-4d6f-bf1a-3733d9f6fa8e", // 章节 ID
            "fromNovel": "94bcb21d-9e05-4509-b048-ffc582184d1f", // 小说 ID
            "title": "章标题",
            "sections": [
                {
                    "uniqueId": "43c8d320-047c-4af6-877b-bfe228841cd1", // 文章（小节）ID
                    "fromChapter": "b8e99ad0-d269-4d6f-bf1a-3733d9f6fa8e", // 所属的章节 ID
                    "title": "第 1 节"
                },
                {
                    // 还可能有更多
                }
            ]
        },
        {
            // 还可能有更多
        }
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
|activateCode|String|邮箱激活码，如果用户未激活，则使用此激活码激活|是|

登录时会检查用户邮箱验证情况，当且仅当邮箱未激活（如 管理员批量创建用户）时，进行如下步骤  
1. 第一次尝试登陆，不带 `activateCode` 参数，
服务端返回错误代码 `USER_UNVERIFIED` 并发送验证码，
此时页面提示账号需要激活，验证码已发送，并展示验证码输入框  
2. 第二次登录，带上 `activateCode` 参数，若校验成功，则登录成功，否则重试第 1 步（注意需要间隔 30秒)

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
>请求格式：`application/x-www-form-urlencoded`

|字段|类型|含义或值|可空|
|---|---|------------|---|
|all|boolean|也包括其他设备，默认 false|是|

若注销成功，服务端返回状态码 204 - No Content

--------

### 用户管理相关接口

#### 修改帐号信息

```
POST $URL/api/account/edit
```

>权限：若为管理员，可修改所有帐号，否则只能修改自己的帐号

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uuid|UUID|需要修改的用户 UID，默认为自己|T|
|userName|String|用户名|T|
|password|String|密码|T|
|email|String|邮箱|T|
|phone|String|手机号|T|
|status|String|账号状态，（参见上方 #数据格式 Account->status）|T|
|vip|Boolean|是否为高级会员|T|
|permission|String|用户权限，（参见上方 #数据格式 Account->permission）|T|
|profileImgUrl|String|头像 URL|T|
|activateCode|String|邮箱验证码，当且仅当修改邮箱、密码时验证|T|

不包含的属性不会修改  
未发生任何改动则认为修改失败  
注意：修改邮箱时验证码的收件帐号为新邮箱  
注意：管理员修改自己的帐号时也需要验证邮箱
注意：管理员不能自己修改自己的账号状态、权限、vip

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

#### 根据用户 ID 查询用户信息

```
GET $URL/api/account/{uniqueId}/info
```

参数：

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uniqueId|UUID|用户 ID，注意要包含在路径里，不是 JSON 参数|F|

返回参数：

```json5
{
    "uid": "b6623a2e62334e36a1b131b4642e81ab", // 用户 ID 的数字格式（十六进制）
    "vip": false,
    "level": 2, // 用户等级
    "profileImgUrl": null, // 用户头像的 URL
    "userName": "admin"
}
```

#### 用户管理

1. 获取用户管理的总页数

```
GET $URL/api/admin/accounts/pages
```  

>请求格式：`application/x-www-form-urlencoded`  
>权限：管理员

|字段|类型|含义或值|可空|
|---|---|------------|---|
|per_page|查询参数 int|每页展示的帐号数量，默认为 20，最大为 100|是|

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
GET $URL/api/admin/accounts/get
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

3. 管理员批量创建用户

```
POST $URL/api/admin/account/add
```

>权限：管理员

请求参数：包含下表属性的 JSON 对象的数组

|字段|类型|含义或值|可空|
|---|---|------------|---|
|userName|String|用户名|F|
|password|String|密码|F|
|email|String|邮箱|F|
|phone|String|手机号|F|
|status|String|账号状态，（参见上方 #数据格式 Account->status）|T 默认 UNVERIFIED|
|vip|Boolean|是否为高级会员|T 默认为 false|
|permission|String|用户权限，（参见上方 #数据格式 Account->permission）|T 默认 USER|

示例：  
```json5
[
  {
    "userName": "合法的",
    "password": "12345678",
    "email": "test@email.test",
    "phone": "15660724871",
    "status": "FINE",
    "permission": "USER",
    "vip": false
  },
  {
    "userName": "默认为未激活",
    "password": "12345678",
    "email": "test@email.test.com",
    "phone": "15660724871"
  },
  {
    "userName": "非法的",
    "password": "密码",
    "email": "test.notvalid",
    "phone": "15660????"
  },
  {
    // 还可能有更多
  }
]
```

若请求成功，服务端返回状态码 200 - OK （即使所有操作都失败）

返回参数有些复杂，但是看了例子就能明白
```json5
{
  "success": [ // 所有成功的请求，为 Account 的数组
    {
      "uid": "ed58afe8-d7a7-4387-97d1-b4e5b6437350",
      "userName": "合法的",
      // ... 省略
      "status": "FINE",
      "permission": "USER",
      "vip": false
    },
    {
      "uid": "5c574a65-6933-4807-b609-c5a6cef07bf6",
      "userName": "默认为未激活",
      // ... 省略
      "status": "UNVERIFIED",
      "permission": "USER",
      "vip": false
    },
    {
      // 还可能有更多
    }
  ],
  "failed": [ // 失败的请求与失败原因的数组
    {
      "request": { // 失败的原请求
        "userName": "非法的",
        "password": "密码",
        "email": "test.notvalid",
        "phone": "15660????",
        "status": "UNVERIFIED",
        "vip": false,
        "permission": "USER"
      },
      "error": { // 此请求失败的原因
        "error": "ILLEGAL_ARGUMENT",
        "errorMessage": "password: 个数必须在6和2147483647之间;\nphone: 个数必须在11和14之间;\nemail: 不是一个合法的电子邮件地址",
        "cause": "调试信息"
      }
    },
    {
      // 还可能有更多
      "request": {/* xxx */},
      "error": {/* xxx */}
    }
  ]
}
```

4. 批量删除用户

```
DELETE $URL/api/admin/account/delete
```

>权限：管理员

请求参数：用户 ID 的 JSON 的数组

示例：
```json5
[
  "ed58afe8-d7a7-4387-97d1-b4e5b6437350",
  "5c574a65-6933-4807-b609-c5a6cef07bf6"
  // 还可能有更多
]
```

若请求成功，服务端返回状态码 200 - OK （即使所有操作都失败）

返回参数和上一步类似，示例：
```json5
{
  "success": [
    {
      "uid": "ed58afe8-d7a7-4387-97d1-b4e5b6437350",
      "userName": "合法的",
      "email": "test@email.test",
      "phone": "15660724871",
      // ... 省略
    },
    {
      // 还可能有更多
    }
  ],
  "failed": [
    {
      "request": "ed58afe8-d7a7",
      "error": {
        "error": "ILLEGAL_ARGUMENT",
        "errorMessage": "Invalid UUID string: ed58afe8-d7a7- ",
        "cause": "调试信息"
      }
    },
    {
      "request": "5c574a65-6933-4807-b609-c5a6cef07bf6",
      "error": {
        "error": "DATABASE_EXCEPTION",
        "errorMessage": "找不到该用户",
        "cause": "调试信息"
      }
    },
    {
      // 还可能有更多
      "request": "UUID",
      "error": {/* xxx */}
    }
  ]
}
```

5. 修改帐号积分
   
```
POST $URL/api/admin/account/experience/edit
```

>权限：管理员

|字段|类型|含义或值|可空|
|---|---|------------|---|
|accountId|UUID|需要修改的用户 UID|F|
|experience|int64|经验值|F|

若修改成功，服务端返回状态码 204 - No Content

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

#### 编辑小说 - 获取原数据

1. 重新获取小说的目录信息

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

2. 获取小说的某一小节，包含文本

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

#### 编辑小说

1. 编辑小说信息

```
POST $URL/api/novel/{novelId}/edit
```

>权限：上传者、管理员

参数：

|字段|类型|含义或值|可空|
|---|---|------------|---|
|novelId|UUID|小说 ID，注意要包含在路径里，不是 query 参数|F|
|copyright|String|<a title="NO_COPYRIGHT 公版,REPRINT 转载,FAN_FICTION 同人,ORIGINAL 原创">版权</a>字符串枚举|T|
|title|String|标题|T|
|authors|String 数组|作者(JSON 数组)|T|
|description|String|简介|T|
|tags|String 数组|用于分类的标签(JSON 数组)|T|
|series|String|<a title="比如《三体》、《黑暗森林》、《死神永生》属于“地球往事三部曲”系列">系列</a>|T|
|publisher|String|出版社|T|
|pubdate|String|出版日期，格式："yyyy年MM月dd日 HH:mm:ss"|T|
|coverImgUrl|String|封面图像 URL|T|
|chapters|UUID 数组|所有章节的 ID（JSON 数组），可以根据这里调整顺序（排序算法未完成）|T|

示例：
```json5
{
  "title": "修改标题",
  "authors": ["作者A","作者B"],
  "chapters": [
    "b8e99ad0-d269-4d6f-bf1a-3733d9f6fa8e"
    // 还可能有更多
  ]
}
```

不包含的属性不会修改  
未发生任何改动则认为修改失败  

若修改成功，服务端返回状态码 204 - No Content

2. 编辑章节信息

```
POST $URL/api/novel/chapter/{chapterId}/edit
```

>权限：上传者、管理员

参数：

|字段|类型|含义或值|可空|
|---|---|------------|---|
|sectionId|UUID|章节 ID，注意要包含在路径里，不是 query 参数|F|
|title|String|标题|T|
|sections|UUID 数组|所有章节的 ID（JSON 数组），可以根据这里调整顺序|T|

不包含的属性不会修改  
未发生任何改动则认为修改失败

若修改成功，服务端返回状态码 204 - No Content

3. 编辑小节

```
POST $URL/api/novel/section/{sectionId}/edit
```

>权限：上传者、管理员

参数：

|字段|类型|含义或值|可空|
|---|---|------------|---|
|sectionId|UUID|小节 ID，注意要包含在路径里，不是 query 参数|F|
|title|String|标题|T|
|text|String|修改内容|T|

不包含的属性不会修改  
未发生任何改动则认为修改失败

若修改成功，服务端返回状态码 204 - No Content

#### 删除小说

1. 删除小说（递归）

```
DELETE $URL/api/novel/{uniqueId}
```

>权限：上传者、管理员

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uniqueId|UUID|小说 ID，注意要包含在路径里，不是 query 参数|F|

若操作成功，服务端返回状态码 202 - ACCEPTED

2. 根据章节 ID 删除某一章（递归）

```
DELETE $URL/api/novel/chapter/{uniqueId}
```

>权限：上传者、管理员

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uniqueId|UUID|章节 ID，注意要包含在路径里，不是 query 参数|F|

若操作成功，服务端返回状态码 202 - ACCEPTED

3. 根据小节 ID 删除某一小节

```
DELETE $URL/api/novel/section/{uniqueId}
```

>权限：上传者、管理员

|字段|类型|含义或值|可空|
|---|---|------------|---|
|uniqueId|UUID|小节 ID，注意要包含在路径里，不是 query 参数|F|

若操作成功，服务端返回状态码 202 - ACCEPTED

#### 搜索小说

1. 查询所有的标签（tag）

```
GET $URL/api/novel/tags/all
```

若查询成功，服务端返回状态码 200 - OK

返回参数：
```json5
[
    "测试",
    "古典文学",
    "外国小说",
]
```

2. 查询所有的系列

```
GET $URL/api/novel/series/all
```

若查询成功，服务端返回状态码 200 - OK

返回参数：  
```json5
[
    "系列",
    "水浒传",
    "三国演义",
    "简·爱",
    "地球往事三部曲"
]
```

3. 获取某用户上传的小说的总页数

```
GET $URL/api/novel/user/{accountId}/pages
```  

>请求格式：`application/x-www-form-urlencoded`  

|字段|类型|含义或值|可空|
|---|---|------------|---|
|accountId|UUID|账号 ID，注意要包含在路径里，不是 query 参数|F|
|per_page|查询参数 int|每页展示的小说数量，默认为 20|是|

若查询成功，返回状态码为 200 - OK

返回参数：

```json5
{
  "per_page" : 20, // 每页展示的小说数量 
  "pages": 1 //总共的页数
}
```

4. 获取某用户上传的所有小说

```
GET $URL/api/novel/user/{accountId}/get
```  

>请求格式：`application/x-www-form-urlencoded`  

|字段|类型|含义或值|可空|
|---|---|------------|---|
|accountId|UUID|账号 ID，注意要包含在路径里，不是 query 参数|F|
|per_page|查询参数 int|每页展示的小说数量，默认为 20|T|
|page|查询参数 int|页码|F|
|sort_by|String|排序方式枚举，见下表，下同|T 默认 HOT_DESC|

排序方式  
编码：UTF-8

|枚举常量|说明|
|----|----|
|BEAT_MATCH|最佳匹配（搜索时有效）|
|AUTHORS|根据作者排序|
|AUTHORS_DESC|作者倒序，下同理|
|COPYRIGHT|版权|
|COPYRIGHT_DESC| |
|HOT|热度|
|HOT_DESC| |
|PUBDATE|发布日期|
|PUBDATE_DESC| |
|PUBLISHER|出版社或发布者|
|PUBLISHER_DESC| |
|RATING|评分|
|RATING_DESC| |
|SERIES|系列|
|SERIES_DESC| |
|TAGS|分类标签|
|TAGS_DESC| |
|TITLE|标题|
|TITLE_DESC| |
|UPLOADER|上传者 ID|
|UPLOADER_DESC| |
|RANDOM|随机|

若查询成功，返回状态码为 200 - OK

返回参数：  
```json5
[
    {
        // 小说目录信息，见上方 #数据格式 NovelIndex
        "chapters": [] // 此处不会含有实际信息，只有章节 ID的列表，因此可以统计章节总数但不能获取章节标题
    },
    {
        // 还可能有更多
    }
]
```

若该页没有数据，服务端返回状态码 204 - No Content

5. 获取本站所有小说的总页数

```
GET $URL/api/admin/novel/all/pages
```  

>请求格式：`application/x-www-form-urlencoded`  

|字段|类型|含义或值|可空|
|---|---|------------|---|
|per_page|查询参数 int|每页展示的小说数量，默认为 20|是|

若查询成功，返回状态码为 200 - OK

返回参数：

```json5
{
  "per_page" : 20, // 每页展示的小说数量 
  "pages": 1 //总共的页数
}
```

6. 获取本站所有小说

```
GET $URL/api/admin/novel/all
```  

>请求格式：`application/x-www-form-urlencoded`  

|字段|类型|含义或值|可空|
|---|---|------------|---|
|per_page|查询参数 int|每页展示的小说数量，默认为 20|T|
|page|查询参数 int|页码|F|
|sort_by|String|排序方式枚举|T 默认 HOT_DESC|

若查询成功，返回状态码为 200 - OK

返回参数：  
```json5
[
    {
        // 小说目录信息，见上方 #数据格式 NovelIndex
        "chapters": [] // 此处不会含有实际信息，只有章节 ID的列表，因此可以统计章节总数但不能获取章节标题
    },
    {
        // 还可能有更多
    }
]
```

若该页没有数据，服务端返回状态码 204 - No Content
