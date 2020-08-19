/*
 *  Copyright (C) 2020 Future Studio
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.wlgzs.futurenovel.exception;

import lombok.Getter;

/**
 * 网站异常基类
 */
public class FutureNovelException extends RuntimeException {

    private static final long serialVersionUID = 6941311654559280755L;

    @Getter
    private final Error error;

    public FutureNovelException(String message) {
        super(message);
        error = Error.UNDEFINED;
    }

    public FutureNovelException(String message, Throwable throwable) {
        super(message, throwable);
        error = Error.UNDEFINED;
    }

    public FutureNovelException(Error error, String message, Throwable throwable) {
        super(message, throwable);
        this.error = error;
    }

    public FutureNovelException(Error error) {
        super(error.getErrorMessage());
        this.error = error;
    }

    public FutureNovelException(Error error, String msg) {
        super(msg);
        this.error = error;
    }

    public enum Error {
        UNDEFINED(500, "未定义"),
        ILLEGAL_ARGUMENT(400, "非法参数"),
        INVALID_TOKEN(403, "未登录"),
        INVALID_PASSWORD(403, "用户名或密码错误"),
        HACK_DETECTED(423, "检测到违规行为"),
        ACCESS_DENIED(403, "拒绝访问"),
        USER_EXIST(409, "用户已存在"),
        USER_UNVERIFIED(403, "账号未激活"),
        WRONG_ACTIVATE_CODE(400, "验证码错误"),
        DATABASE_EXCEPTION(500, "数据库异常"),
        PERMISSION_DENIED(403, "无权操作"),
        FILE_TOO_LARGE(413, "上传的文件过大"),
        NOVEL_NOT_FOUND(404, "找不到小说"),
        ITEM_NOT_FOUND(404, "项目不存在"),
        BOOK_SELF_NOT_FOUND(404, "找不到该收藏夹"),
        EXP_NOT_ENOUGH(403, "积分不足");
        private final String msg;
        private final int statusCode;
        Error(int statusCode, String msg) {
            this.statusCode = statusCode;
            this.msg = msg;
        }

        public String getErrorMessage() {
            return msg;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
