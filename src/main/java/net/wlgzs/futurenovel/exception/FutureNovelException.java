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

    public FutureNovelException() {
        error = Error.UNDEFINED;
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
        UNDEFINED(418, "未定义"),
        ILLEGAL_ARGUMENT(400, "非法参数"),
        INVALID_TOKEN(403, "未登录"),
        INVALID_PASSWORD(403, "用户名或密码错误"),
        HACK_DETECTED(423, "检测到违规行为"),
        ACCESS_DENIED(403, "拒绝访问"),
        USER_EXIST(409, "用户已存在"),
        WRONG_ACTIVATE_CODE(400, "验证码错误");
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
