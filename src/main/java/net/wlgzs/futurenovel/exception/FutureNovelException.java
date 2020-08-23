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

import java.util.Locale;
import java.util.Optional;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * 网站业务异常（以及非业务异常的包装）
 */
public class FutureNovelException extends RuntimeException {

    private static final long serialVersionUID = 6941311654559280755L;

    @Getter
    private final Error error;

    private final Locale locale;

    private MessageSource messageSource;

    public FutureNovelException(String message) {
        this(message, null);
    }

    public FutureNovelException(String message, Throwable throwable) {
        this(Error.UNDEFINED, message, throwable);
    }

    public FutureNovelException(Error error, String message, Throwable throwable) {
        super(message, throwable);
        this.error = error;
        this.locale = LocaleContextHolder.getLocale();
        var attr = RequestContextHolder.currentRequestAttributes();
        if (attr instanceof ServletRequestAttributes) {
            var context = RequestContextUtils.findWebApplicationContext(((ServletRequestAttributes) attr).getRequest());
            if (context != null) messageSource = context.getBean(MessageSource.class);
        }
    }

    public FutureNovelException(Error error) {
        this(error, error.getErrorMessageCode(), null);
    }

    public FutureNovelException(Error error, String msg) {
        this(error, msg, null);
    }

    @Override
    public String getLocalizedMessage() {
        return Optional.ofNullable(getMessage())
            .filter(s -> s.startsWith("error."))
            .map(s -> {
                if (messageSource != null) return messageSource.getMessage(s, null, s, locale);
                else return s;
            })
            .orElse(getMessage());
    }

    public enum Error {
        UNDEFINED(500),
        ILLEGAL_ARGUMENT(400),
        INVALID_TOKEN(403),
        INVALID_PASSWORD(403),
        HACK_DETECTED(423),
        ACCESS_DENIED(403),
        USER_EXIST(409),
        USER_UNVERIFIED(403),
        WRONG_ACTIVATE_CODE(400),
        DATABASE_EXCEPTION(500),
        PERMISSION_DENIED(403),
        FILE_TOO_LARGE(413),
        NOVEL_NOT_FOUND(404),
        ITEM_NOT_FOUND(404),
        BOOK_SHELF_NOT_FOUND(404),
        EXP_NOT_ENOUGH(403);
        private final String code;
        private final int statusCode;

        Error(int statusCode) {
            this.statusCode = statusCode;
            this.code = "error." + name().toLowerCase();
        }

        public String getErrorMessageCode() {
            return code;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
