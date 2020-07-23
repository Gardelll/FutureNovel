package net.wlgzs.futurenovel.controller;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.bean.ErrorResponse;
import net.wlgzs.futurenovel.bean.RegisterRequest;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.service.AccountService;
import net.wlgzs.futurenovel.service.EmailService;
import net.wlgzs.futurenovel.service.FileService;
import net.wlgzs.futurenovel.service.NovelService;
import net.wlgzs.futurenovel.service.TokenStore;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * HTML 页面的控制器
 */
@Controller
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
public class TemplateController extends AbstractAppController {

    public TemplateController(TokenStore tokenStore,
                              AccountService accountService,
                              EmailService emailService,
                              NovelService novelService,
                              Validator defaultValidator,
                              Properties futureNovelConfig,
                              FileService fileService) {
        super(tokenStore, accountService, emailService, novelService, defaultValidator, futureNovelConfig, fileService);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(defaultValidator);
    }

    /**
     * 主页路由
     * @param uid Cookie：用户 ID
     * @param tokenStr Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request Http 请求
     * @param session Http Session
     * @param model 模板属性
     * @return 对应的视图
     */
    @GetMapping({"", "/", "/index"})
    public String home(@CookieValue(name = "uid", defaultValue = "") String uid,
                       @CookieValue(name = "token", defaultValue = "") String tokenStr,
                       @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                       HttpServletRequest request,
                       HttpSession session,
                       Model model) {
        var account = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        if (account != null) {
            log.info("LoggedIn, account={}", account.getEmail());
            // 已登录
            model.addAttribute("currentAccount", account);
        }
        return "FutureNovel";
    }

    @GetMapping("/register")
    public String register(Model m, @CookieValue(name = "uid", defaultValue = "") String uid,
                           @CookieValue(name = "token", defaultValue = "") String tokenStr,
                           @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                           HttpServletRequest request,
                           HttpSession session) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        m.addAttribute("errorMessage", "OK");
        return "register";
    }

    @GetMapping({"/login"})
    public String login() {
        return "login";
    }

    /**
     * 注册表单路由
     * @param req 请求参数，详见 {@link RegisterRequest}
     * @param m 模板属性
     * @param request Http 请求
     * @param response Http 响应
     * @param session Session 服务端变量
     * @return 含有错误信息的视图或跳转响应
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String register(@Valid RegisterRequest req,
                           BindingResult bindingResult,
                           Model m,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           HttpSession session) {
        m.addAttribute("errorMessage", "OK");
        try {
            if (bindingResult.hasErrors()) {
                var errMsgBuilder = new StringBuilder();
                bindingResult.getFieldErrors().forEach(fieldError -> errMsgBuilder.append(fieldError.getField())
                        .append(": ")
                        .append(fieldError.getDefaultMessage())
                        .append(";\n"));
                errMsgBuilder.delete(errMsgBuilder.length() - 2, errMsgBuilder.length());
                throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, errMsgBuilder.toString());
            }
            if (!req.password.equals(req.passwordRepeat))
                throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, "密码输入不一致");
            if (accountService.isEmailUsed(req.email, null))
                throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, "邮箱地址(" + req.email + ")已被使用");
            if (accountService.isEmailUsed(req.userName, null))
                throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, "用户名(" + req.userName + ")已被使用");
            if (req.email.equals(session.getAttribute("activateEmail")) &&
                    req.activateCode.equalsIgnoreCase((String) session.getAttribute("activateCode")) &&
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
                    Account.Status.FINE,
                    false,
                    Account.Permission.USER,
                    0,
                    null);
                accountService.register(account);
                var uidCookie = new Cookie("uid", account.getUid().toString());
                uidCookie.setMaxAge((int) Duration.ofDays(365).toSeconds());
                uidCookie.setPath(request.getContextPath());
                response.addCookie(uidCookie);
                session.setAttribute("activateCode", null);
                session.setAttribute("activateBefore", null);
                session.setAttribute("activateEmail", null);
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

    @GetMapping("/novel/view")
    public String bookView(@CookieValue(name = "uid", defaultValue = "") String uid,
                           @CookieValue(name = "token", defaultValue = "") String tokenStr,
                           @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                           HttpServletRequest request,
                           HttpSession session) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        return "look-book";
    }

    @GetMapping("/novel/read")
    public String bookRead(@CookieValue(name = "uid", defaultValue = "") String uid,
                           @CookieValue(name = "token", defaultValue = "") String tokenStr,
                           @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                           HttpServletRequest request,
                           HttpSession session) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        return "read-book";
    }

    @GetMapping("/search")
    public String search(@CookieValue(name = "uid", defaultValue = "") String uid,
                         @CookieValue(name = "token", defaultValue = "") String tokenStr,
                         @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                         HttpServletRequest request,
                         HttpSession session) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        return "rank";
    }

    @GetMapping("/novel/write")
    public String novelWrite(@CookieValue(name = "uid", defaultValue = "") String uid,
                             @CookieValue(name = "token", defaultValue = "") String tokenStr,
                             @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                             HttpServletRequest request,
                             HttpSession session) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, true);
        return "writer";
    }

    @GetMapping("/novel/create")
    public String novelCreate(@CookieValue(name = "uid", defaultValue = "") String uid,
                              @CookieValue(name = "token", defaultValue = "") String tokenStr,
                              @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                              HttpServletRequest request,
                              HttpSession session) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, true);
        return "WorkInformation";
    }

    @GetMapping({"/admin/", "/admin", "/admin/index"})
    public String adminHome(@CookieValue(name = "uid", defaultValue = "") String uid,
                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                            HttpServletRequest request,
                            HttpSession session) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);
        return "index";
    }

    @ExceptionHandler(Exception.class)
    public String error(Model model, HttpServletResponse response, Exception e) {
        log.error("error: {}", e.getLocalizedMessage());
        ErrorResponse responseErr = buildErrorResponse(e);
        model.addAttribute("status", responseErr.status);
        model.addAttribute("error", responseErr.error);
        model.addAttribute("cause", responseErr.cause);
        model.addAttribute("errorMessage", responseErr.errorMessage);
        response.setStatus(responseErr.status);
        return "error";
    }

    @Override
    @RequestMapping(path = "/error")
    public void handle(HttpServletRequest request) {
       super.handle(request);
    }

}
