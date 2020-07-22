package net.wlgzs.futurenovel.controller;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.bean.EditAccountRequest;
import net.wlgzs.futurenovel.bean.ErrorResponse;
import net.wlgzs.futurenovel.bean.LoginRequest;
import net.wlgzs.futurenovel.bean.SendCaptchaRequest;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.filter.DefaultFilter;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.service.AccountService;
import net.wlgzs.futurenovel.service.EmailService;
import net.wlgzs.futurenovel.service.FileService;
import net.wlgzs.futurenovel.service.TokenStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Ajax 接口相关的控制器
 */
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
public class ApiController extends AbstractAppController {

    public ApiController(TokenStore tokenStore,
                         AccountService accountService,
                         EmailService emailService,
                         Validator defaultValidator,
                         Properties futureNovelConfig,
                         FileService fileService) {
        super(tokenStore, accountService, emailService, defaultValidator, futureNovelConfig, fileService);
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
            used = accountService.isEmailUsed(name, null);
        } else used = accountService.isUsernameUsed(name, null);
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
    @ResponseBody
    public Map<String, ?> login(@RequestBody @Valid LoginRequest req,
                                @RequestHeader(value = "User-Agent") String userAgent,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                HttpSession session) {
        var account = accountService.login(req.userName, req.password);
        var token = tokenStore.acquireToken(account, request.getRemoteAddr(), userAgent);

        // 更新帐号属性
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

        // 保存到 session 和设置 cookie
        session.setAttribute("currentAccount", account);
        var uidCookie = new Cookie("uid", account.getUid().toString());
        uidCookie.setMaxAge((int) Duration.ofDays(365).toSeconds());
        uidCookie.setPath(request.getContextPath());
        response.addCookie(uidCookie);
        var tokenCookie = new Cookie("token", token.getToken());
        tokenCookie.setMaxAge((int) Duration.ofDays(Long.parseLong(futureNovelConfig.getProperty("future.token.cookieExpire", "30"))).toSeconds());
        tokenCookie.setPath(request.getContextPath());
        response.addCookie(tokenCookie);
        String redirectTo = Optional.ofNullable(req.redirectTo)
            .filter(s -> !s.isEmpty())
            .orElseGet(() -> Optional.ofNullable((String) session.getAttribute("redirectTo"))
                .filter(s -> !s.isEmpty())
                .orElse(request.getContextPath()));
        session.setAttribute("redirectTo", null);
        //ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectTo).body(null);
        return Map.ofEntries(
                Map.entry("redirectTo", redirectTo),
                Map.entry("account", account)
        );
    }

    /**
     * 注销
     * @param uid Cookie：用户 ID
     * @param tokenStr Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request Http 请求
     * @param response Http 响应
     * @param session Session 服务端变量
     */
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
     * 编辑用户信息
     * 不包含的属性不会修改
     * 未发生任何改动则认为修改失败
     * @param req 请求参数 {@link EditAccountRequest}
     * @param uid Cookie：用户 ID
     * @param tokenStr Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request Http 请求
     * @param session Session 服务端变量
     * @see EditAccountRequest
     */
    @PostMapping(value = "/account/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editAccount(@RequestBody @Valid EditAccountRequest req,
                            @CookieValue(name = "uid", defaultValue = "") String uid,
                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                            HttpServletRequest request,
                            HttpSession session) {
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();

        if (req.uuid == null || req.uuid.isBlank()) req.uuid = uid;
        req.uid = UUID.fromString(req.uuid);

        // 检查冲突
        if (req.email != null && accountService.isEmailUsed(req.email, req.uid))
            throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, "邮箱地址(" + req.email + ")已被使用");
        if (req.userName != null && accountService.isEmailUsed(req.userName, req.uid))
            throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, "用户名(" + req.userName + ")已被使用");

        // 检查验证码
        if (currentAccount.getUid().equals(req.uid) && (req.email != null || req.password != null)) {
            if (((req.email != null) && !req.email.equals(session.getAttribute("activateEmail"))) ||
                    !req.activateCode.equalsIgnoreCase((String) session.getAttribute("activateCode")) ||
                    Optional.ofNullable((Long) session.getAttribute("activateBefore"))
                            .map(value -> System.currentTimeMillis() > value).orElse(true)) {
                throw new FutureNovelException(FutureNovelException.Error.WRONG_ACTIVATE_CODE);
            } else {
                session.setAttribute("activateCode", null);
                session.setAttribute("activateBefore", null);
                session.setAttribute("activateEmail", null);
            }
        }

        if (!accountService.editAccount(currentAccount, req)) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "修改失败");
        }
    }

    /**
     * 上传图片接口
     * @param file 文件
     * @param uid Cookie：用户 ID
     * @param tokenStr Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request Http 请求
     * @return json 信息
     */
    @PutMapping(value = "/img/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, ?> uploadImage(@RequestParam("file") MultipartFile file,
                                      @CookieValue(name = "uid", defaultValue = "") String uid,
                                      @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                      @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                      HttpServletRequest request) {
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();

        Path tmpFile = null;
        try (var in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image.getWidth() > 4096 || image.getHeight() > 4096) throw new FutureNovelException(FutureNovelException.Error.FILE_TOO_LARGE);
            // TODO 简化操作，这里拷贝了三次
            tmpFile = Files.createTempFile("uploadTemp", ".jpg");
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
            image = newImage;
            if (!ImageIO.write(image, "JPEG", tmpFile.toFile()))
                throw new FutureNovelException(FutureNovelException.Error.UNDEFINED);
            try (var jpegIn = Files.newInputStream(tmpFile)) {
                var index = fileService.saveFile(jpegIn);
                String url;
                try {
                    url = new URI(request.getScheme(),
                            null,
                            request.getServerName(),
                            request.getLocalPort(),
                            request.getContextPath() + "/api/img/" + index,
                            null,
                            null)
                            .normalize()
                            .toString();
                } catch (URISyntaxException ignored) {
                    url = request.getRequestURL().toString().replaceAll("/img/upload", "/img/" + index);
                }
                return Map.ofEntries(Map.entry("md5", index),
                        Map.entry("url", url),
                        Map.entry("mime", MediaType.IMAGE_JPEG_VALUE));
            }
        } catch (IOException e) {
            throw new FutureNovelException("文件上传失败", e);
        } finally {
            if (tmpFile != null) try { Files.delete(tmpFile); } catch (IOException ignored) {}
        }
    }

    @GetMapping(value = "/img/{hash:[a-f0-9]{32}}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity<ByteArrayResource> getImage(@PathVariable("hash") String hash) {
        try (var stream = fileService.readFile(hash)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(stream.available())
                    .eTag(hash)
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                    .body(new ByteArrayResource(stream.readAllBytes()));
        } catch (IOException e) {
            return ResponseEntity.notFound()
                    .build();
        }
    }

    /**
     * 获取用户管理的总页数
     * 权限：管理员
     * @param perPage 每页显示的页数，默认20
     * @param uid Cookie：用户 ID
     * @param tokenStr Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request Http 请求
     * @return json 数据
     */
    @GetMapping("/admin/accounts/pages")
    @ResponseBody
    public Map<String, ?> accountAdminPages(@RequestParam(name = "per_page", defaultValue = "20", required = false) int perPage,
                                            @CookieValue(name = "uid", defaultValue = "") String uid,
                                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                            HttpServletRequest request) {
        if (perPage <= 0) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);

        return Map.ofEntries(
                Map.entry("per_page", perPage),
                Map.entry("pages", accountService.getAllAccountPages(perPage)),
                Map.entry("timestamp", System.currentTimeMillis())
        );
    }

    /**
     * 获取所有的用户信息
     * 权限：管理员
     * 若无数据，返回状态码 204
     * @param page 第几页
     * @param perPage 每页显示的页数，默认20
     * @param uid Cookie：用户 ID
     * @param tokenStr Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request Http 请求
     * @return json 数据
     */
    @GetMapping("/admin/accounts/get")
    @ResponseBody
    public ResponseEntity<List<Account>> accountAdminGet(
            @RequestParam(name = "page") int page,
            @RequestParam(name = "per_page", defaultValue = "20", required = false) int perPage,
            @CookieValue(name = "uid", defaultValue = "") String uid,
            @CookieValue(name = "token", defaultValue = "") String tokenStr,
            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
            HttpServletRequest request) {
        if (page <= 0 || perPage <= 0) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);

        var result = accountService.getAllAccount(page, perPage);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .build();
        } else {
            return ResponseEntity.ok(result);
        }
    }

//    // TODO 统计网站数据
//    public Map<String, ?> statistics() {
//
//    }

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
            emailService.sendEmail(req.email, "FutureNovel 邮箱地址验证",
                String.format("您好，<br /><p>您收到这封邮件，是由于在 `%s` 进行了新用户注册，或用户修改 Email 使用了这个邮箱地址。" +
                        "如果您并没有访问过本站，或没有进行上述操作，请忽略这封邮件。您不需要退订或进行其他进一步的操作。</p><br />" +
                        "----------------------------------------------------------------------<br />" +
                        "<p>您的邮箱验证码为：<h3>%s</h3> 10分钟内有效。</p>" +
                        "----------------------------------------------------------------------<br />" +
                        "<p>请在表单中填写该信息</p>",
                        new URI(request.getScheme(),
                                null,
                                request.getServerName(),
                                request.getServerPort(),
                                request.getContextPath(),
                                null,
                                null)
                                .normalize()
                                .toString(),
                        activateCode));
            session.setAttribute("activateCode", activateCode);
            session.setAttribute("activateBefore", System.currentTimeMillis() + Duration.ofMinutes(10).toMillis());
            session.setAttribute("activateEmail", req.email);
            DefaultFilter.blockIp(request.getRemoteAddr(), "/api/sendCaptcha", 30); // 30 秒后重试
        } catch (MailException | MessagingException | URISyntaxException e) {
            throw new FutureNovelException(e.getLocalizedMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> error(Exception e) {
        log.error("error: {}", e.getLocalizedMessage());
        var response = buildErrorResponse(e);
        return ResponseEntity.status(response.status)
                .body(response);
    }

}
