package net.wlgzs.futurenovel.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.wlgzs.futurenovel.bean.ErrorResponse;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.service.AccountService;
import net.wlgzs.futurenovel.service.EmailService;
import net.wlgzs.futurenovel.service.FileService;
import net.wlgzs.futurenovel.service.NovelService;
import net.wlgzs.futurenovel.service.TokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public abstract class AbstractAppController {

    protected final TokenStore tokenStore;

    protected final AccountService accountService;

    protected final EmailService emailService;

    protected final NovelService novelService;

    protected final FileService fileService;

    protected final Validator defaultValidator;

    protected final Properties futureNovelConfig;

    protected String serverUrl = null;

    public AbstractAppController(TokenStore tokenStore,
                                 AccountService accountService,
                                 EmailService emailService,
                                 NovelService novelService,
                                 Validator defaultValidator,
                                 Properties futureNovelConfig,
                                 FileService fileService) {
        this.tokenStore = tokenStore;
        this.accountService = accountService;
        this.emailService = emailService;
        this.defaultValidator = defaultValidator;
        this.futureNovelConfig = futureNovelConfig;
        this.fileService = fileService;
        this.novelService = novelService;
    }

    protected boolean safeRedirect(String redirectTo) {
        String serverUrl = this.serverUrl == null ?
                           this.serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                               .build()
                               .normalize()
                               .toString() :
                           this.serverUrl;
        if (!redirectTo.startsWith("http") && !redirectTo.startsWith("//")) {
            return true;
        }
        return redirectTo.startsWith(serverUrl);
    }

    /**
     * 检查登录状态
     *
     * @param uid            用户 ID
     * @param tokenStr       token 令牌
     * @param clientIp       用户 IP 地址
     * @param userAgent      浏览器 UA
     * @param throwException 登陆失败是否要抛出异常(uncheck)
     * @return 登陆的用户
     */
    protected Account checkLogin(@NonNull String uid,
                                 @NonNull String tokenStr,
                                 @NonNull String clientIp,
                                 @NonNull String userAgent,
                                 boolean throwException) {
        Optional<Account> account = Optional.of(uid)
                .filter(s -> !s.isBlank())
                .map(s -> tokenStore.verifyToken(tokenStr, UUID.fromString(uid), clientIp, userAgent))
                .flatMap(token -> Optional.ofNullable(accountService.getAccount(UUID.fromString(uid))));
        if (throwException) {
            return account.orElseThrow(() -> new FutureNovelException(FutureNovelException.Error.INVALID_TOKEN));
        } else {
            return account.orElse(null);
        }
    }

    /**
     * 检查登录状态并设置或清除 session
     *
     * @param uid            用户 ID
     * @param tokenStr       token 令牌
     * @param clientIp       用户 IP 地址
     * @param userAgent      浏览器 UA
     * @param session        Http Session
     * @param throwException 登陆失败是否要抛出异常(uncheck)
     * @return 登陆的用户
     */
    protected Account checkLoginAndSetSession(@NonNull String uid,
                                              @NonNull String tokenStr,
                                              @NonNull String clientIp,
                                              @NonNull String userAgent,
                                              @NonNull HttpSession session,
                                              boolean throwException) {
        Account account = checkLogin(uid, tokenStr, clientIp, userAgent, throwException);
        session.setAttribute("currentAccount", account);
        return account;
    }

    protected ErrorResponse buildErrorResponse(Exception e) {
        ErrorResponse response = new ErrorResponse();
        if (e instanceof HttpMessageNotReadableException ||
                e instanceof HttpMessageNotWritableException ||
                e instanceof IllegalArgumentException ||
                e instanceof ServletRequestBindingException) {
            var errorMessage = e.getLocalizedMessage();
            if (errorMessage == null || errorMessage.isBlank())
                errorMessage = FutureNovelException.Error.ILLEGAL_ARGUMENT.getErrorMessage();
            e = new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, errorMessage, e);
        }
        response.errorMessage = e.getLocalizedMessage();
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        stringWriter.flush();
        response.cause = stringWriter.toString();
        if (e instanceof FutureNovelException) {
            response.error = ((FutureNovelException) e).getError().name();
            response.status = ((FutureNovelException) e).getError().getStatusCode();
        } else if (e instanceof MethodArgumentNotValidException || e instanceof BindException) {
            List<FieldError> result;
            if (e instanceof MethodArgumentNotValidException)
                result = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors();
            else
                result = ((BindException) e).getBindingResult().getFieldErrors();
            var errMsgBuilder = new StringBuilder();
            result.forEach(fieldError -> errMsgBuilder.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append(";\n"));
            errMsgBuilder.delete(errMsgBuilder.length() - 2, errMsgBuilder.length());
            response.errorMessage = errMsgBuilder.toString();
            response.error = FutureNovelException.Error.ILLEGAL_ARGUMENT.name();
            response.status = HttpStatus.BAD_REQUEST.value();
        } else if (e instanceof ResponseStatusException) {
            response.error = ((ResponseStatusException) e).getStatus().name();
            response.status = ((ResponseStatusException) e).getStatus().value();
        } else if (e instanceof HttpStatusCodeException) {
            response.error = ((HttpStatusCodeException) e).getStatusCode().name();
            response.status = ((HttpStatusCodeException) e).getStatusCode().value();
        } else {
            response.error = e.getClass().getSimpleName();
            response.status = HttpStatus.I_AM_A_TEAPOT.value();
        }
        return response;
    }

    public void handle(HttpServletRequest request) {
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        int status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (errorMessage != null && !errorMessage.isBlank())
            throw new ResponseStatusException(HttpStatus.valueOf(status), errorMessage);
        else throw new ResponseStatusException(HttpStatus.valueOf(status));
    }

}
