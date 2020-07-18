package net.wlgzs.futurenovel.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.bean.LoginRequest;
import net.wlgzs.futurenovel.bean.SendCaptchaRequest;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.service.AccountService;
import net.wlgzs.futurenovel.service.EmailService;
import net.wlgzs.futurenovel.service.TokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*",
        methods = {
                RequestMethod.POST,
                RequestMethod.GET
        },
        allowedHeaders = {
                "Authorization",
                "Content-Type",
                "*"
        }) // TODO 临时解决分离调试跨域问题
@Slf4j
public class ApiController {

    private final TokenStore tokenStore;

    private final AccountService accountService;

    private final EmailService emailService;

    private final Validator defaultValidator;

    public ApiController(TokenStore tokenStore, AccountService accountService, EmailService emailService, Validator defaultValidator) {
        this.tokenStore = tokenStore;
        this.accountService = accountService;
        this.emailService = emailService;
        this.defaultValidator = defaultValidator;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(defaultValidator);
    }

    /**
     * 用于检查用户名或邮箱是否存在
     * 若不存在，返回状态码 204
     * @param name 需要检查的值
     * @param type email 或 username, 默认为 username
     */
    @GetMapping("/checkUsername")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkUsername(@RequestParam(name = "name") String name, @RequestParam(defaultValue = "username", required = false) String type) {
        boolean used;
        if ("email".equalsIgnoreCase(type)) {
            used = accountService.isEmailUsed(name);
        } else used = accountService.isUsernameUsed(name);
        if (used) throw new FutureNovelException(FutureNovelException.Error.USER_EXIST);
    }

    /**
     * 登录接口
     * 登录成功后设置 Cookie 并跳转到请求参数中 redirectTo 所指向的页面
     * 若请求参数不包含 redirectTo, 则使用 Session 变量中的值，或者跳转到首页
     * @param req 请求参数，请参阅 {@link LoginRequest}
     * @param userAgent 浏览器 UA, 用于生成 Token
     * @param request Http 请求
     * @param response Http 响应
     * @param session Session 服务端变量
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void login(@RequestBody @Valid LoginRequest req,
                           @RequestHeader(value = "User-Agent") String userAgent,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           HttpSession session) {
        var account = accountService.login(req.userName, req.password);
        var token = tokenStore.acquireToken(account, request.getRemoteAddr(), userAgent);
        account.setLastLoginIP(request.getRemoteAddr());
        Optional.ofNullable(account.getLastLoginDate()).filter(date -> {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"));
            var lastLogin = Calendar.getInstance();
            lastLogin.setTime(date);
            int day = lastLogin.get(Calendar.DAY_OF_YEAR);
            int year = lastLogin.get(Calendar.YEAR);
            var now = Calendar.getInstance();
            now.setTimeInMillis(System.currentTimeMillis());
            return now.get(Calendar.YEAR) != year || now.get(Calendar.DAY_OF_YEAR) != day;
        }).ifPresent((date) -> account.setExperience(account.getExperience() + 3));
        account.setLastLoginDate(new Date());
        accountService.updateAccount(account);
        session.setAttribute("currentAccount", account);
        var uidCookie = new Cookie("uid", account.getUid().toString());
        uidCookie.setMaxAge((int) Duration.ofDays(30).toSeconds());
        uidCookie.setPath(request.getContextPath());
        response.addCookie(uidCookie);
        var tokenCookie = new Cookie("token", token.getToken());
        tokenCookie.setMaxAge((int) Duration.ofDays(30).toSeconds());
        tokenCookie.setPath(request.getContextPath());
        response.addCookie(tokenCookie);
        String redirectTo = Optional.ofNullable(req.redirectTo)
            .filter(s -> !s.isEmpty())
            .orElseGet(() -> Optional.ofNullable((String) session.getAttribute("redirectTo"))
                .filter(s -> !s.isEmpty())
                .orElse("../"));
        //ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectTo).body(null);
        try {
            response.sendRedirect(redirectTo);
        } catch (IOException e) {
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, e.getLocalizedMessage());
        }
    }

    @GetMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CookieValue(name = "uid", defaultValue = "") String uid,
                       @CookieValue(name = "token", defaultValue = "") String tokenStr,
                       @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       HttpSession session) {
        log.debug("uid={}, token={}, agent={}, ip={}", uid, tokenStr, userAgent, request.getRemoteAddr());
        var token = uid.isEmpty() ? null : tokenStore.verifyToken(tokenStr, UUID.fromString(uid), request.getRemoteAddr(), userAgent);
        if (token == null) throw new FutureNovelException(FutureNovelException.Error.INVALID_TOKEN);
        session.setAttribute("currentAccount", null);
        var tokenCookie = new Cookie("token", null);
        tokenCookie.setMaxAge(0);
        tokenCookie.setPath(request.getContextPath());
        response.addCookie(tokenCookie);
        tokenStore.removeToken(token);
    }

    /**
     * 请求注册验证码
     * @param session Session 服务端变量
     * @param req 请求参数，包含 email 字段
     */
    @PostMapping(value = "/sendCaptcha", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendCaptcha(HttpServletRequest request, HttpSession session, @Valid @RequestBody SendCaptchaRequest req) {
        // TODO 对此接口添加图片验证码
        String activateCode;
        var random = new Random();
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        activateCode = stringBuilder.toString();
        try {
            emailService.sendEmail(req.email, "FutureNovel 地址验证",
                String.format("您好，<br /><p>您收到这封邮件，是由于在 `%s` 进行了新用户注册，或用户修改 Email 使用了这个邮箱地址。如果您并没有访问过本站，或没有进行上述操作，请忽略这封邮件。您不需要退订或进行其他进一步的操作。</p><br />" +
                        "----------------------------------------------------------------------<br />" +
                        "<p>您的邮箱验证码为：<h3>%s</h3> 10分钟内有效。</p>" +
                        "----------------------------------------------------------------------<br />" +
                        "<p>请在表单中填写该信息</p>", new URI(request.getScheme(), null, request.getServerName(), request.getLocalPort(), request.getContextPath(), null, null).normalize().toString(), activateCode));
            session.setAttribute("activateCode", activateCode);
            session.setAttribute("activateBefore", System.currentTimeMillis() + Duration.ofMinutes(10).toMillis());
            session.setAttribute("activateEmail", req.email);
        } catch (MailException | MessagingException | URISyntaxException e) {
            throw new FutureNovelException(e.getLocalizedMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<?, ?>> error(Exception e) {
        log.error("error: {}", e.getLocalizedMessage());
        if (e instanceof HttpMessageNotReadableException || e instanceof IllegalArgumentException) {
            e = new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        }
        HashMap<String, String> body = new HashMap<>();
        body.put("errorMessage", e.getLocalizedMessage());
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        stringWriter.flush();
        body.put("cause", stringWriter.toString());
        if (e instanceof FutureNovelException) {
            body.put("error", ((FutureNovelException) e).getError().name());
            return ResponseEntity.status(((FutureNovelException) e).getError().getStatusCode())
                .body(body);
        } else if (e instanceof MethodArgumentNotValidException) {
            var result = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors();
            var errMsgBuilder = new StringBuilder();
            result.forEach(fieldError -> errMsgBuilder.append(fieldError.getField())
                .append(": ")
                .append(fieldError.getDefaultMessage())
                .append(";\n"));
            errMsgBuilder.delete(errMsgBuilder.length() - 2, errMsgBuilder.length());
            body.put("errorMessage", errMsgBuilder.toString());
            body.put("error", "ILLEGAL_ARGUMENT");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body);
        } else if (e instanceof ResponseStatusException) {
            body.put("error", ((ResponseStatusException) e).getStatus().name());
            return ResponseEntity.status(((ResponseStatusException) e).getStatus())
                .body(body);
        } else {
            body.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
                .body(body);
        }
    }

}
