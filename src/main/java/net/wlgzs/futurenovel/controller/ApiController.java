package net.wlgzs.futurenovel.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final TokenStore tokenStore;

    private final AccountService accountService;

    private final EmailService emailService;

    public ApiController(TokenStore tokenStore, AccountService accountService, EmailService emailService) {
        this.tokenStore = tokenStore;
        this.accountService = accountService;
        this.emailService = emailService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void login(@Valid @RequestBody LoginRequest req,
                           @RequestHeader(value = "User-Agent") String userAgent,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           HttpSession session) {
        var account = accountService.login(req.userName, req.password);
        var token = tokenStore.acquireToken(account, request.getRemoteAddr(), userAgent);
        account.setLastLoginIP(request.getRemoteAddr());
        Optional.ofNullable(account.getLastLoginDate()).filter(date -> {
            var lastLogin = Calendar.getInstance();
            lastLogin.setTime(date);
            int day = lastLogin.get(Calendar.DAY_OF_YEAR);
            int year = lastLogin.get(Calendar.YEAR);
            var now = Calendar.getInstance();
            now.setTimeInMillis(System.currentTimeMillis());
            return now.get(Calendar.YEAR) != year && now.get(Calendar.DAY_OF_YEAR) != day;
        }).ifPresent((date) -> account.setExperience(account.getExperience() + 3));
        account.setLastLoginDate(new Date());
        accountService.updateAccount(account);
        session.setAttribute("currentAccount", account);
        var uidCookie = new Cookie("uid", account.getUid().toString());
        uidCookie.setMaxAge((int) Duration.ofDays(30).toSeconds());
        response.addCookie(uidCookie);
        var tokenCookie = new Cookie("token", token.getToken());
        tokenCookie.setMaxAge((int) Duration.ofDays(30).toSeconds());
        response.addCookie(tokenCookie);
        String redirectTo = Optional.ofNullable(req.redirectTo)
            .filter(s -> !s.isEmpty())
            .orElseGet(() -> Optional.ofNullable((String) session.getAttribute("redirectTo"))
                .filter(s -> !s.isEmpty())
                .orElse("/"));
        //ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectTo).body(null);
        try {
            response.sendRedirect(redirectTo);
        } catch (IOException e) {
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, e.getLocalizedMessage());
        }
    }

    @GetMapping(value = "/sendCaptcha", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendCaptcha(HttpSession session, @Valid @RequestBody SendCaptchaRequest req) {
        // TODO 对此接口添加图片验证码
        String activateCode;
        var random = new Random();
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        activateCode = stringBuilder.toString();
        emailService.sendEmail(req.email, "FutureNovel", "您注册 FutureNovel 的验证码[请勿回复]", String.format("您的注册验证码为：%s, 10分钟内有效。<br >如果您未进行注册，请忽略此邮件", activateCode));
        session.setAttribute("activateCode", activateCode);
        session.setAttribute("activateBefore", System.currentTimeMillis() + Duration.ofMinutes(10).toMillis());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<?, ?>> error(Exception e) {
        log.error("error:", e);
        HashMap<String, String> body = new HashMap<>();
        body.put("errorMessage", e.getLocalizedMessage());
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        stringWriter.flush();
        body.put("cause", stringWriter.toString());
        if (e instanceof FutureNovelException) {
            body.put("error", ((FutureNovelException) e).getError().toString());
            return ResponseEntity.status(((FutureNovelException) e).getError().getStatusCode())
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
