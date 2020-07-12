package net.wlgzs.futurenovel.controller;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.bean.RegisterRequest;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.service.AccountService;
import net.wlgzs.futurenovel.service.TokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

@Controller
@Slf4j
public class TemplateController {

    private final TokenStore tokenStore;

    private final AccountService accountService;

    private final Validator defaultValidator;

    public TemplateController(TokenStore tokenStore, AccountService accountService, Validator defaultValidator) {
        this.tokenStore = tokenStore;
        this.accountService = accountService;
        this.defaultValidator = defaultValidator;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(defaultValidator);
    }

    @GetMapping({"", "/", "/index"})
    public String home(@CookieValue(name = "uid", defaultValue = "") String uid,
                       @CookieValue(name = "token", defaultValue = "") String tokenStr,
                       @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                       HttpServletRequest request,
                       HttpSession session,
                       Model model) {
        var token = uid.isEmpty() ? null : tokenStore.verifyToken(tokenStr, UUID.fromString(uid), request.getRemoteAddr(), userAgent);
        if (token != null) {
            var account = Optional.ofNullable((Account) session.getAttribute("currentAccount")).orElseGet(() -> {
                var a = accountService.getAccount(UUID.fromString(uid));
                session.setAttribute("currentAccount", a);
                return a;
            });
            log.info("LoggedIn, account={}", account.getEmail());
            // 已登录
            model.addAttribute("currentAccount", account);
        }
        return "FutureNovel";
    }

    @GetMapping("/register")
    public String register(Model m) {
        m.addAttribute("errorMessage", "OK");
        return "register";
    }

    @PostMapping(value = "/register", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public String register(Model m,
                           @RequestBody @Valid RegisterRequest req,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           HttpSession session) {
        m.addAttribute("errorMessage", "OK");
        try {
            if (!req.password.equals(req.passwordRepeat))
                throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, "密码输入不一致");
            if (req.activateCode.equalsIgnoreCase((String) session.getAttribute("activateCode")) &&
                Optional.ofNullable((Long) session.getAttribute("activateBefore"))
                    .map(value -> System.currentTimeMillis() < value).orElse(false)) {
                var account = new Account(UUID.randomUUID(),
                    req.userName,
                    BCrypt.hashpw(req.password, BCrypt.gensalt()),
                    req.email,
                    req.phone,
                    request.getRemoteAddr(),
                    null,
                    new Date(),
                    null,
                    Account.Status.FINED,
                    Account.Permission.USER,
                    0,
                    null);
                accountService.register(account);
                var uidCookie = new Cookie("uid", account.getUid().toString());
                uidCookie.setMaxAge((int) Duration.ofDays(30).toSeconds());
                uidCookie.setPath(request.getContextPath());
                response.addCookie(uidCookie);
                session.setAttribute("regSuccessRedirect", Boolean.TRUE);
                if (req.redirectTo != null) session.setAttribute("redirectTo", req.redirectTo);
                return "redirect:index";
            } else throw new FutureNovelException(FutureNovelException.Error.WRONG_ACTIVATE_CODE);
        } catch (FutureNovelException e) {
            m.addAttribute("errorMessage", e.getLocalizedMessage());
            m.addAttribute("error", e.getError().toString());
            m.addAttribute("cause", e);
            response.setStatus(e.getError().getStatusCode());
            log.warn("注册失败，原因：{}", e.getLocalizedMessage());
        }
        return "register";
    }

    @ExceptionHandler(Exception.class)
    public String error(Model model, HttpServletResponse response, Exception e) {
        log.error("error: {}", e.getLocalizedMessage());
        if (e instanceof HttpMessageNotReadableException || e instanceof IllegalArgumentException) {
            e = new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        }
        model.addAttribute("errorMessage", e.getLocalizedMessage());
        model.addAttribute("cause", e);
        if (e instanceof FutureNovelException) {
            model.addAttribute("error", ((FutureNovelException) e).getError().toString());
            response.setStatus(((FutureNovelException) e).getError().getStatusCode());
        } else if (e instanceof MethodArgumentNotValidException) {
            var result = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors();
            var errMsgBuilder = new StringBuilder();
            result.forEach(fieldError -> errMsgBuilder.append(fieldError.getField())
                .append(": ")
                .append(fieldError.getDefaultMessage())
                .append(";\n"));
            errMsgBuilder.delete(errMsgBuilder.length() - 2, errMsgBuilder.length());
            model.addAttribute("errorMessage", errMsgBuilder.toString());
            model.addAttribute("error", "ILLEGAL_ARGUMENT");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        } else if (e instanceof ResponseStatusException) {
            model.addAttribute("error", ((ResponseStatusException) e).getStatus().name());
            response.setStatus(((ResponseStatusException) e).getStatus().value());
        } else {
            model.addAttribute("error", e.getClass().getSimpleName());
            response.setStatus(HttpStatus.I_AM_A_TEAPOT.value());
        }
        return "error";
    }
}
