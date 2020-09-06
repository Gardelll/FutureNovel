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

package net.wlgzs.futurenovel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.AppProperties;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.filter.DefaultFilter;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.BookSelf;
import net.wlgzs.futurenovel.model.Chapter;
import net.wlgzs.futurenovel.model.Comment;
import net.wlgzs.futurenovel.model.NovelIndex;
import net.wlgzs.futurenovel.model.ReadHistory;
import net.wlgzs.futurenovel.model.Section;
import net.wlgzs.futurenovel.packet.Requests;
import net.wlgzs.futurenovel.packet.Responses;
import net.wlgzs.futurenovel.service.AccountService;
import net.wlgzs.futurenovel.service.BookSelfService;
import net.wlgzs.futurenovel.service.CommentService;
import net.wlgzs.futurenovel.service.EmailService;
import net.wlgzs.futurenovel.service.FileService;
import net.wlgzs.futurenovel.service.NovelService;
import net.wlgzs.futurenovel.service.ReadHistoryService;
import net.wlgzs.futurenovel.service.SettingService;
import net.wlgzs.futurenovel.service.TokenStore;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Ajax 接口相关的控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*",
    methods = {
        RequestMethod.POST,
        RequestMethod.GET,
        RequestMethod.PUT,
        RequestMethod.DELETE
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
                         NovelService novelService,
                         ReadHistoryService readHistoryService,
                         CommentService commentService,
                         Validator defaultValidator,
                         AppProperties futureNovelConfig,
                         FileService fileService,
                         BookSelfService bookSelfService,
                         ObjectMapper objectMapper,
                         SettingService settingService,
                         MessageSource messageSource) {
        super(tokenStore, accountService, emailService, novelService, readHistoryService, commentService, defaultValidator, futureNovelConfig, fileService, bookSelfService, objectMapper, settingService, messageSource);
    }

    /**
     * 用于检查用户名或邮箱是否存在
     * <p>
     * 若不存在，返回状态码 204
     *
     * @param name 需要检查的值
     * @param type email 或 username, 默认为 username
     */
    @GetMapping("/checkUsername")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkUsername(@RequestParam(name = "name") String name, @RequestParam(defaultValue = "username", required = false) String type) {
        if (name.isBlank()) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        boolean used;
        if ("email".equalsIgnoreCase(type)) {
            used = accountService.isEmailUsed(name, null);
        } else used = accountService.isUsernameUsed(name, null);
        if (used) throw new FutureNovelException(FutureNovelException.Error.USER_EXIST);
    }

    /**
     * 登录接口
     * <p>
     * 登录成功后设置 Cookie 并跳转到请求参数中 redirectTo 所指向的页面
     * <p>
     * 若请求参数不包含 redirectTo, 则使用 Session 变量中的值，或者跳转到首页
     *
     * @param req       请求参数，请参阅 {@link Requests.LoginRequest}
     * @param userAgent 浏览器 UA, 用于生成 Token
     * @param request   Http 请求
     * @param response  Http 响应
     * @param session   Session 服务端变量
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, ?> login(@RequestBody @Valid Requests.LoginRequest req,
                                @RequestHeader(value = "User-Agent") String userAgent,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                HttpSession session) {
        var account = accountService.login(req.userName, req.password);

        // 若账号未验证
        if (account.getStatus() == Account.Status.UNVERIFIED) {
            if (req.activateCode == null) {
                // 1. 第一次尝试登陆，提示未验证并发送验证码
                var sendCaptchaRequest = new Requests.SendCaptchaRequest();
                sendCaptchaRequest.email = account.getEmail();
                sendCaptcha(request, session, sendCaptchaRequest);
                throw new FutureNovelException(FutureNovelException.Error.USER_UNVERIFIED);
            } else if (account.getEmail().equals(session.getAttribute("activateEmail")) &&
                req.activateCode.equalsIgnoreCase((String) session.getAttribute("activateCode")) &&
                Optional.ofNullable((Long) session.getAttribute("activateBefore"))
                    .map(value -> System.currentTimeMillis() < value).orElse(false)) {
                // 2. 第二次登录，带上了验证码参数，并校验成功，设为激活状态
                session.setAttribute("activateCode", null);
                session.setAttribute("activateBefore", null);
                session.setAttribute("activateEmail", null);
                account.setStatus(Account.Status.FINE);
            } else {
                // 3. 激活失败，继续上一步
                throw new FutureNovelException(FutureNovelException.Error.WRONG_ACTIVATE_CODE);
            }
        } else if (account.getStatus() == Account.Status.BANED) {
            throw new FutureNovelException(FutureNovelException.Error.ACCESS_DENIED);
        }

        var token = tokenStore.acquireToken(account, request.getRemoteAddr(), userAgent);

        // 更新帐号属性
        account.setLastLoginIP(request.getRemoteAddr());
        Optional.ofNullable(account.getLastLoginDate()).filter(date -> {
            var lastLogin = Calendar.getInstance();
            lastLogin.setTime(date);
            int day = lastLogin.get(Calendar.DAY_OF_YEAR);
            int year = lastLogin.get(Calendar.YEAR);
            var now = Calendar.getInstance();
            now.setTimeInMillis(System.currentTimeMillis());
            return now.get(Calendar.YEAR) != year || now.get(Calendar.DAY_OF_YEAR) != day;
        }).ifPresent((date) -> account.setExperience(account.getExperience().add(BigInteger.valueOf(3))));
        account.setLastLoginDate(new Date());
        if (!accountService.updateAccount(account))
            log.warn("用户 {} 的数据更新失败", account.getUserName());

        // 保存到 session 和设置 cookie
        session.setAttribute("currentAccount", account);
        var uidCookie = new Cookie("uid", account.getUid().toString());
        uidCookie.setMaxAge((int) Duration.ofDays(365).toSeconds());
        uidCookie.setPath(getBaseUri(request));
        response.addCookie(uidCookie);
        var tokenCookie = new Cookie("token", token.getToken());
        tokenCookie.setMaxAge((int) Duration.ofDays(futureNovelConfig.getToken().getCookieExpire()).toSeconds());
        tokenCookie.setPath(getBaseUri(request));
        response.addCookie(tokenCookie);
        String redirectTo = Optional.ofNullable(req.redirectTo)
            .filter(s -> !s.isEmpty())
            .orElseGet(() -> Optional.ofNullable((String) session.getAttribute("redirectTo"))
                .filter(s -> !s.isEmpty())
                .filter(this::safeRedirect)
                .orElse(getBaseUri(request)));
        session.setAttribute("redirectTo", null);
        //ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectTo).body(null);
        return Map.ofEntries(
            Map.entry("redirectTo", redirectTo),
            Map.entry("account", account)
        );
    }

    /**
     * 注销
     *
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     * @param response  Http 响应
     * @param session   Session 服务端变量
     */
    @GetMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestParam(defaultValue = "false") boolean all,
                       @CookieValue(name = "uid", defaultValue = "") String uid,
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
        tokenCookie.setPath(getBaseUri(request));
        response.addCookie(tokenCookie);
        if (all) tokenStore.removeAll(token.getAccountUid());
        else tokenStore.removeToken(token);
    }

    /**
     * 编辑用户信息
     * <p>
     * 不包含的属性不会修改
     * <p>
     * 未发生任何改动则认为修改失败
     *
     * @param req       请求参数 {@link Requests.EditAccountRequest}
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     * @param session   Session 服务端变量
     * @see Requests.EditAccountRequest
     */
    @PostMapping(value = "/account/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editAccount(@RequestBody @Valid Requests.EditAccountRequest req,
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
            throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, getMessage("register.email_already_in_use", req.email));
        if (req.userName != null && accountService.isEmailUsed(req.userName, req.uid))
            throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, getMessage("register.username_already_in_use", req.userName));

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
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("error.can_not_modify"));
        }
    }

    @PostMapping(value = "/admin/account/experience/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editAccountExperience(@RequestBody @Valid Requests.EditExperienceRequest req,
                                      @CookieValue(name = "uid", defaultValue = "") String uid,
                                      @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                      @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                      HttpServletRequest request) {
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);

        Account account = accountService.getAccount(UUID.fromString(req.accountId));
        account.setExperience(req.experience);
        accountService.updateAccountExperience(account);
    }

    @GetMapping("/account/{uniqueId:[0-9a-f\\-]{36}}/info")
    public Map<String, ?> getAccountShortInfo(@PathVariable String uniqueId) {
        Account account = accountService.getAccount(UUID.fromString(uniqueId));
        Map<String, Object> result = new HashMap<>();
        result.put("userName", account.getUserName());
        result.put("uid", account.getUidNum());
        result.put("vip", account.isVIP());
        result.put("profileImgUrl", account.getProfileImgUrl());
        result.put("level", account.getLevel());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 管理员批量添加用户
     *
     * @param reqs      请求参数，一个列表
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     * @return json 信息
     * @see Requests.AddAccountRequest
     */
    @PostMapping(value = "/admin/account/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Map<String, List<?>> adminAddAccount(@RequestBody List<Requests.AddAccountRequest> reqs,
                                                @CookieValue(name = "uid", defaultValue = "") String uid,
                                                @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                                @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                                HttpServletRequest request) {
        if (reqs == null || reqs.isEmpty())
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("error.empty_argument"));
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);

        LinkedList<Account> success = new LinkedList<>();
        LinkedList<Map<String, ?>> failed = new LinkedList<>();

        for (Requests.AddAccountRequest req : reqs) {
            try {
                var e = new BindException(req, "list#request");
                defaultValidator.validate(req, e);
                if (e.hasErrors()) throw e;
                if (accountService.isEmailUsed(req.email, null))
                    throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, "邮箱地址(" + req.email + ")已被使用");
                if (accountService.isEmailUsed(req.userName, null))
                    throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, "用户名(" + req.userName + ")已被使用");

                var account = new Account(UUID.randomUUID(),
                    req.userName,
                    BCrypt.hashpw(req.password, BCrypt.gensalt()),
                    req.email,
                    req.phone,
                    request.getRemoteAddr(),
                    null,
                    new Date(),
                    null,
                    req.status,
                    req.vip,
                    req.permission,
                    BigInteger.ZERO,
                    null);
                accountService.register(account);
                success.add(account);
            } catch (Exception e) {
                failed.add(Map.of(
                    "request", req,
                    "error", buildErrorResponse(e)
                ));
            }
        }

        return Map.of(
            "success", success,
            "failed", failed
        );
    }

    /**
     * 管理员批量删除用户
     *
     * @param uids      要删除的 uid 的列表
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     * @return json 信息
     */
    @DeleteMapping(value = "/admin/account/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Map<String, List<?>> adminDeleteAccount(@RequestBody List<String> uids,
                                                   @CookieValue(name = "uid", defaultValue = "") String uid,
                                                   @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                                   @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                                   HttpServletRequest request) {
        if (uids == null || uids.isEmpty())
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("error.empty_argument"));
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);

        LinkedList<Account> success = new LinkedList<>();
        LinkedList<Map<String, ?>> failed = new LinkedList<>();

        for (String uidStr : uids) {
            try {
                UUID uuid = UUID.fromString(uidStr);
                if (uid.equals(uuid.toString())) throw new IllegalArgumentException(getMessage("admin.can_not_delete_self_account"));
                Account account = accountService.getAccount(uuid);
                if (accountService.unRegister(account) != 1)
                    throw new FutureNovelException(FutureNovelException.Error.ITEM_NOT_FOUND, getMessage("admin.user_not_exist"));
                success.add(account);
                commentService.clearAccountComment(account.getUid());
                readHistoryService.clearReadHistory(account, null, null);
            } catch (Exception e) {
                failed.add(Map.of(
                    "request", uidStr,
                    "error", buildErrorResponse(e)
                ));
            }
        }
        return Map.of(
            "success", success,
            "failed", failed
        );
    }

    /**
     * 上传图片接口
     *
     * @param file      文件
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     * @return json 信息
     */
    @PutMapping(value = "/img/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, ?> uploadImage(@RequestParam("file") MultipartFile file,
                                      @CookieValue(name = "uid", defaultValue = "") String uid,
                                      @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                      @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                      HttpServletRequest request) {
        if (getServerUrl().matches("^https?://(localhost|127.0.0.1|\\[::1]).+")) throw new FutureNovelException(getMessage("upload.localhost_prohibit"));

        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();

        if (file.isEmpty()) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("upload.empty_file"));

        try (ByteArrayOutputStream tmpStream = new ByteArrayOutputStream(); var in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image.getWidth() > 4096 || image.getHeight() > 4096)
                throw new FutureNovelException(FutureNovelException.Error.FILE_TOO_LARGE);
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
            image = newImage;
            if (!ImageIO.write(image, "JPEG", tmpStream))
                throw new FutureNovelException(FutureNovelException.Error.UNDEFINED);
            try (var jpegIn = new ByteArrayInputStream(tmpStream.toByteArray())) {
                var index = fileService.saveFile(jpegIn);
                String url = ServletUriComponentsBuilder.fromCurrentRequestUri()
                    .build()
                    .normalize()
                    .toString().replaceAll("/img/upload", "/img/" + index);
                return Map.ofEntries(Map.entry("md5", index),
                    Map.entry("url", url),
                    Map.entry("mime", MediaType.IMAGE_JPEG_VALUE));
            }
        } catch (IOException e) {
            throw new FutureNovelException(getMessage("upload.failed"), e);
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
     *
     * @param perPage   每页显示的页数，默认20
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     * @return json 数据
     */
    @GetMapping("/admin/accounts/pages")
    @ResponseBody
    public Map<String, ?> accountAdminPages(@RequestParam(name = "per_page", defaultValue = "20", required = false) int perPage,
                                            @CookieValue(name = "uid", defaultValue = "") String uid,
                                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                            HttpServletRequest request) {
        if (perPage <= 0 || perPage > 100) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
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
     *
     * @param page      第几页
     * @param perPage   每页显示的页数，默认20
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
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
        if (page <= 0 || perPage <= 0 || perPage > 100)
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
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

    /**
     * 小说管理
     * <p>
     * 创建小说信息
     * <p>
     * 权限：参见 {@link net.wlgzs.futurenovel.model.Account.Permission}
     *
     * @param req       请求参数，参见 {@link Requests.CreateNovelIndexRequest}
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     * @return 章节 json 对象
     * @see net.wlgzs.futurenovel.model.Account.Permission
     * @see Requests.CreateNovelIndexRequest
     * @see NovelIndex
     */
    @PostMapping(value = "/novel/addIndex", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public NovelIndex addNovelIndex(@RequestBody @Valid Requests.CreateNovelIndexRequest req,
                                    @CookieValue(name = "uid", defaultValue = "") String uid,
                                    @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                    @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                    HttpServletRequest request) {
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        req.authors.replaceAll(String::trim);
        req.tags.replaceAll(String::trim);
        req.tags = req.tags.stream().filter(s -> s.length() <= 3).collect(Collectors.toList());
        if (req.tags.isEmpty())
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("valid.field_not_valid", "tags"));
        if (req.series == null || req.series.isBlank()) req.series = req.title;
        return novelService.createNovelIndex(currentAccount,
            req.copyright,
            req.title.trim(),
            req.authors,
            safeHTML(req.description),
            (byte) 0,
            req.tags,
            req.series,
            req.publisher,
            req.pubdate,
            req.coverImgUrl);
    }

    /**
     * 小说管理
     * <p>
     * 添加章节目录
     * <p>
     * 权限：上传者或管理员
     *
     * @param fromNovel 小说的 ID
     * @param req       请求参数，仅包含 标题
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     * @return 章节 json 对象
     * @see Chapter
     */
    @PostMapping("/novel/{fromNovel:[0-9a-f\\-]{36}}/addChapter")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Chapter addChapter(@PathVariable("fromNovel") String fromNovel,
                              @RequestBody Requests.AddChapterRequest req,
                              @CookieValue(name = "uid", defaultValue = "") String uid,
                              @CookieValue(name = "token", defaultValue = "") String tokenStr,
                              @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                              HttpServletRequest request) {
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        NovelIndex novelIndex = novelService.getNovelIndex(UUID.fromString(fromNovel));
        return novelService.addChapter(currentAccount, novelIndex, req.title);
    }

    /**
     * 小说管理
     * <p>
     * 添加小节
     * <p>
     * 权限：上传者或管理员
     *
     * @param fromNovel   小说的 ID
     * @param fromChapter 章节的 ID，只需前 8 位
     * @param req         请求参数，包含标题和文本
     * @param uid         Cookie：用户 ID
     * @param tokenStr    Cookie：登陆令牌
     * @param userAgent   Header：浏览器标识
     * @param request     Http 请求
     * @return 小节 json 对象，不含文本
     */
    @PostMapping("/novel/{fromNovel:[0-9a-f\\-]{36}}/{fromChapter:[0-9a-f\\-]{8,36}}/addSection")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> addSection(@PathVariable("fromNovel") String fromNovel,
                                          @PathVariable("fromChapter") String fromChapter,
                                          @RequestBody @Valid Requests.AddSectionRequest req,
                                          @CookieValue(name = "uid", defaultValue = "") String uid,
                                          @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                          @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                          HttpServletRequest request) {
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        NovelIndex novelIndex = novelService.getNovelIndex(UUID.fromString(fromNovel));

        // 拓展 fromChapter
        ArrayNode allChapters = novelIndex.getChapters();
        for (JsonNode uuidStr : allChapters) {
            String uidNum = uuidStr.asText();
            if (uidNum.startsWith(fromChapter)) {
                fromChapter = uuidStr.asText();
                break;
            }
        }
        Chapter chapter = novelService.getChapter(UUID.fromString(fromChapter));
        Section section = novelService.addSection(currentAccount, chapter, novelIndex, req.title, safeHTML(req.text));
        return Map.ofEntries(
            Map.entry("uniqueId", section.getUniqueId().toString()),
            Map.entry("title", section.getTitle())
        );
    }

    /**
     * 获取小说的目录信息
     *
     * @param uniqueId 小说的 ID
     * @return json 对象
     */
    @GetMapping("/novel/{uniqueId:[0-9a-f\\-]{36}}")
    @ResponseBody
    public Responses.Novel getNovelInfo(@PathVariable("uniqueId") String uniqueId) {
        return buildNovel(UUID.fromString(uniqueId));
    }

    /**
     * 获取小说的某一小节，包含文本
     *
     * @param uniqueId 小节的 ID
     * @return json 对象
     */
    @GetMapping("/novel/section/{uniqueId:[0-9a-f\\-]{36}}")
    @ResponseBody
    public Section getSection(@PathVariable String uniqueId) {
        return novelService.getSection(UUID.fromString(uniqueId));
    }

    /**
     * 删除小说（递归）
     * <p>
     * 权限：上传者或管理员
     *
     * @param uniqueId  小说的 ID
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     */
    @DeleteMapping("/novel/{uniqueId:[0-9a-f\\-]{36}}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteNovelIndex(@PathVariable String uniqueId,
                                 @CookieValue(name = "uid", defaultValue = "") String uid,
                                 @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                 @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                 HttpServletRequest request) {
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        NovelIndex novelIndex = novelService.getNovelIndex(UUID.fromString(uniqueId));
        novelService.deleteNovelIndex(currentAccount, novelIndex);
    }

    @DeleteMapping("/novel/chapter/{uniqueId:[0-9a-f\\-]{36}}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteChapter(@PathVariable String uniqueId,
                              @CookieValue(name = "uid", defaultValue = "") String uid,
                              @CookieValue(name = "token", defaultValue = "") String tokenStr,
                              @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                              HttpServletRequest request) {
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        novelService.deleteChapter(currentAccount, UUID.fromString(uniqueId));
    }

    @DeleteMapping("/novel/section/{uniqueId:[0-9a-f\\-]{36}}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteSection(@PathVariable String uniqueId,
                              @CookieValue(name = "uid", defaultValue = "") String uid,
                              @CookieValue(name = "token", defaultValue = "") String tokenStr,
                              @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                              HttpServletRequest request) {
        // 检查权限
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        final UUID sectionId = UUID.fromString(uniqueId);
        novelService.deleteSection(currentAccount, sectionId);
        commentService.clearSectionComment(sectionId);
    }

    @GetMapping("/novel/tags/all")
    @ResponseBody
    public List<String> novelGetAllTags() {
        return List.of(novelService.getTags().toArray(new String[0]));
    }

    @GetMapping("/novel/series/all")
    @ResponseBody
    public List<String> novelGetAllSeries() {
        return List.of(novelService.getSeries().toArray(new String[0]));
    }

    /**
     * 获取本站所有小说
     *
     * @param page    页码
     * @param perPage 每页显示的数量
     * @param sortBy  排序方式，参见 {@link Requests.SearchNovelRequest.SortBy}
     * @return 小说目录的列表（不含章节目录）
     */
    @GetMapping("/admin/novel/all")
    @ResponseBody
    public ResponseEntity<List<NovelIndex>> novelGetAll(@RequestParam(name = "page") int page,
                                                        @RequestParam(name = "per_page", defaultValue = "20") int perPage,
                                                        @RequestParam(name = "sort_by", defaultValue = "HOT_DESC") Requests.SearchNovelRequest.SortBy sortBy) {
        if (page <= 0 || perPage <= 0 || perPage > 100)
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        int offset = (page - 1) * perPage;
        final var result = novelService.getAllNovelIndex(offset, perPage, sortBy.getOrderBy());
        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    /**
     * 获取本站所有小说的总页数
     *
     * @param perPage 每页显示的数量
     * @return json 数据
     */
    @GetMapping("/admin/novel/all/pages")
    @ResponseBody
    public Map<String, ?> novelGetAllPages(@RequestParam(name = "per_page", defaultValue = "20") int perPage) {
        if (perPage <= 0 || perPage > 100) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        long total = novelService.countAllNovelIndex();
        return Map.of(
            "per_page", perPage,
            "pages", countPage(total, perPage),
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 获取某个用户上传的所有小说
     *
     * @param accountId 用户的 ID
     * @param page      页码
     * @param perPage   每页显示的数量
     * @param sortBy    排序方式，参见 {@link Requests.SearchNovelRequest.SortBy}
     * @return 小说目录的列表（不含章节目录）
     */
    @GetMapping("/novel/user/{accountId:[0-9a-f\\-]{36}}/get")
    @ResponseBody
    public ResponseEntity<List<NovelIndex>> novelGetAllFromUser(@PathVariable("accountId") String accountId,
                                                                @RequestParam(name = "page") int page,
                                                                @RequestParam(name = "per_page", defaultValue = "20") int perPage,
                                                                @RequestParam(name = "sort_by", defaultValue = "HOT_DESC") Requests.SearchNovelRequest.SortBy sortBy) {
        if (page <= 0 || perPage <= 0 || perPage > 100)
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        int offset = (page - 1) * perPage;
        final var result = novelService.findNovelIndexByUploader(UUID.fromString(accountId), offset, perPage, sortBy.getOrderBy());
        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    /**
     * 获取某个用户上传的所有小说的总页数
     *
     * @param accountId 用户的 ID
     * @param perPage   每页显示的数量
     * @return json 数据
     */
    @GetMapping("/novel/user/{accountId:[0-9a-f\\-]{36}}/pages")
    @ResponseBody
    public Map<String, ?> novelGetAllPagesFromUser(@PathVariable("accountId") String accountId,
                                                   @RequestParam(name = "per_page", defaultValue = "20") int perPage) {
        if (perPage <= 0 || perPage > 100) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        long total = novelService.countNovelIndexByUploader(UUID.fromString(accountId));
        return Map.of(
            "per_page", perPage,
            "pages", countPage(total, perPage),
            "timestamp", System.currentTimeMillis()
        );
    }

    @PostMapping("/novel/{uniqueId:[0-9a-f\\-]{36}}/edit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editNovelIndex(@PathVariable("uniqueId") String uniqueId,
                               @RequestBody @Valid Requests.EditNovelRequest req,
                               @CookieValue(name = "uid", defaultValue = "") String uid,
                               @CookieValue(name = "token", defaultValue = "") String tokenStr,
                               @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                               HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        if (req.tags != null) {
            req.tags = req.tags.stream().filter(s -> s.length() <= 3).collect(Collectors.toList());
            if (req.tags.isEmpty())
                throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("valid.field_not_valid", "tags"));
        }

        if (!novelService.editNovelIndex(currentAccount, UUID.fromString(uniqueId), req))
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("error.can_not_modify"));
    }

    @PostMapping("/novel/chapter/{uniqueId:[0-9a-f\\-]{36}}/edit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editChapter(@PathVariable("uniqueId") String uniqueId,
                            @RequestBody @Valid Requests.AddChapterRequest req,
                            @CookieValue(name = "uid", defaultValue = "") String uid,
                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                            HttpServletRequest request) {
        if ((req.title == null || req.title.isBlank()) && req.sectionsEdit == null)
            throw new IllegalArgumentException(getMessage("valid.field_can_not_be_empty", "title & sections"));
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        var chapter = novelService.getChapter(UUID.fromString(uniqueId));
        if (req.title != null && !req.title.isBlank()) chapter.setTitle(req.title);
        ArrayNode sections = req.sectionsEdit == null ? null : req.sectionsEdit.stream()
            .filter(s -> s.matches("[0-9a-f\\-]{36}"))
            .collect(
                () -> new ArrayNode(objectMapper.getNodeFactory()),
                ArrayNode::add,
                ArrayNode::addAll
            );
        if (sections != null) {
            if (sections.isEmpty()) throw new IllegalArgumentException(getMessage("novel.edit.no_valid_section"));
            chapter.setSections(sections);
        }
        novelService.editChapter(currentAccount, chapter);
    }

    @PostMapping("/novel/section/{uniqueId:[0-9a-f\\-]{36}}/edit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editSection(@PathVariable("uniqueId") String uniqueId,
                            @RequestBody Requests.AddSectionRequest req,
                            @CookieValue(name = "uid", defaultValue = "") String uid,
                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                            HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        if (req.title != null && req.title.isBlank()) throw new IllegalArgumentException(getMessage("valid.field_can_not_be_empty", "title"));
        if (req.text != null && (req.text.length() < 200 || req.text.length() > 4194304))
            throw new IllegalArgumentException(getMessage("valid.field_size_not_valid", "text", "200B", "4MB"));
        if (req.text != null) req.text = safeHTML(req.text);

        if (!novelService.editSection(currentAccount, UUID.fromString(uniqueId), req))
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("error.can_not_modify"));
    }

    @PostMapping("/novel/section/{sectionId:[0-9a-f\\-]{36}}/comment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addComment(@PathVariable("sectionId") String sectionId,
                           @RequestBody @Valid Requests.AddCommentRequest req,
                           @CookieValue(name = "uid", defaultValue = "") String uid,
                           @CookieValue(name = "token", defaultValue = "") String tokenStr,
                           @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                           HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();

        commentService.addComment(currentAccount.getUid(), UUID.fromString(sectionId), req.rating, req.text);
    }

    @GetMapping("/novel/section/{sectionId:[0-9a-f\\-]{36}}/comment/get")
    @ResponseBody
    public ResponseEntity<List<Responses.CommentInfo>> getCommentFromSection(@PathVariable("sectionId") String sectionId,
                                                                             @RequestParam(name = "page") int page,
                                                                             @RequestParam(name = "per_page", defaultValue = "20") int perPage) {
        if (page <= 0 || perPage <= 0 || perPage > 100)
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        int offset = (page - 1) * perPage;
        var result = commentService.getComments(UUID.fromString(sectionId), offset, perPage);
        if (result.isEmpty()) return ResponseEntity.noContent().build();
        else return ResponseEntity.ok(result);
    }

    @GetMapping("/account/{accountId:[0-9a-f\\-]{36}}/comment/get")
    @ResponseBody
    public ResponseEntity<List<Responses.CommentInfo>> getCommentFromAccount(@PathVariable("accountId") String accountId,
                                                                             @RequestParam(name = "page") int page,
                                                                             @RequestParam(name = "per_page", defaultValue = "20") int perPage) {
        if (page <= 0 || perPage <= 0 || perPage > 100)
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        int offset = (page - 1) * perPage;
        var result = commentService.getCommentsByAccount(UUID.fromString(accountId), offset, perPage);
        if (result.isEmpty()) return ResponseEntity.noContent().build();
        else return ResponseEntity.ok(result);
    }

    @DeleteMapping("/comment/{uniqueId:[0-9a-f\\-]{36}}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteComment(@PathVariable("uniqueId") String uniqueId,
                              @CookieValue(name = "uid", defaultValue = "") String uid,
                              @CookieValue(name = "token", defaultValue = "") String tokenStr,
                              @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                              HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();
        Comment comment = commentService.getComment(UUID.fromString(uniqueId));
        NovelIndex novel = novelService.findNovelIndexBySectionId(comment.getSectionId());
        if (currentAccount.getPermission() == Account.Permission.ADMIN || currentAccount.getUid().equals(novel.getUploader())) {
            commentService.deleteComment(comment.getUniqueId(), null);
        } else {
            commentService.deleteComment(comment.getUniqueId(), currentAccount.getUid());
        }
    }

    @DeleteMapping("/novel/section/{sectionId:[0-9a-f\\-]{36}}/comment/clear")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void clearSectionComment(@PathVariable("sectionId") String sectionId,
                                    @CookieValue(name = "uid", defaultValue = "") String uid,
                                    @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                    @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                    HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        NovelIndex novel = novelService.findNovelIndexBySectionId(UUID.fromString(sectionId));
        if (currentAccount.getUid().equals(novel.getUploader())) currentAccount.checkPermission();
        else currentAccount.checkPermission(Account.Permission.ADMIN);

        commentService.clearSectionComment(UUID.fromString(sectionId));
    }

    @DeleteMapping("/account/{accountId:[0-9a-f\\-]{36}}/comment/clear")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void clearAccountComment(@PathVariable("accountId") String accountId,
                                    @CookieValue(name = "uid", defaultValue = "") String uid,
                                    @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                    @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                    HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        if (currentAccount.getUid().equals(UUID.fromString(accountId))) currentAccount.checkPermission();
        else currentAccount.checkPermission(Account.Permission.ADMIN);

        commentService.clearAccountComment(UUID.fromString(accountId));
    }

    @GetMapping("/comment/getAll")
    @ResponseBody
    public ResponseEntity<List<Responses.CommentInfo>> getAllComment(@RequestParam(name = "page") int page,
                                                                     @RequestParam(name = "per_page", defaultValue = "20") int perPage,
                                                                     @CookieValue(name = "uid", defaultValue = "") String uid,
                                                                     @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                                                     @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                                                     HttpServletRequest request) {
        if (page <= 0 || perPage <= 0 || perPage > 100)
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        int offset = (page - 1) * perPage;
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);
        var result = commentService.getComments(offset, perPage);
        if (result.isEmpty()) return ResponseEntity.noContent().build();
        else return ResponseEntity.ok(result);
    }

    @PostMapping("/account/readHistory/get")
    @ResponseBody
    public ResponseEntity<List<ReadHistory>> getReadHistory(@RequestBody @Valid Requests.GetReadHistoryRequest req,
                                                            @CookieValue(name = "uid", defaultValue = "") String uid,
                                                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                                            HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        var result = readHistoryService.getReadHistory(currentAccount, req.after, req.before);
        if (result.isEmpty()) return ResponseEntity.noContent().build();
        else return ResponseEntity.ok(result);
    }

    @DeleteMapping("/account/readHistory/{uniqueId:[0-9a-f\\-]{36}}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteReadHistory(@PathVariable("uniqueId") String uniqueId,
                                  @CookieValue(name = "uid", defaultValue = "") String uid,
                                  @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                  @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                  HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        readHistoryService.deleteReadHistory(UUID.fromString(uniqueId), currentAccount.getUid());
    }

    @DeleteMapping("/account/readHistory/clear")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void clearReadHistory(@RequestBody @Valid Requests.GetReadHistoryRequest req,
                                 @CookieValue(name = "uid", defaultValue = "") String uid,
                                 @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                 @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                 HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);

        readHistoryService.clearReadHistory(currentAccount, req.after, req.before);
    }

    @PostMapping("/bookSelf/create")
    @ResponseBody
    public BookSelf createBookSelf(@RequestBody @Valid Requests.CreateBookSelfRequest req,
                                   @CookieValue(name = "uid", defaultValue = "") String uid,
                                   @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                   @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                   HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();
        if (req.title == null || req.title.isBlank()) req.title = String.format("%s 的书架", currentAccount.getUserName());
        return bookSelfService.createBookSelf(currentAccount, req.title);
    }

    @DeleteMapping("/bookSelf/{uniqueId:[0-9a-f\\-]{36}}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteBookSelf(@PathVariable("uniqueId") String uniqueId,
                               @CookieValue(name = "uid", defaultValue = "") String uid,
                               @CookieValue(name = "token", defaultValue = "") String tokenStr,
                               @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                               HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();
        bookSelfService.deleteBookSelf(UUID.fromString(uniqueId));
    }

    @GetMapping("/bookSelf/{uniqueId:[0-9a-f\\-]{36}}")
    @ResponseBody
    public BookSelf getBookSelf(@PathVariable("uniqueId") String uniqueId) {
        return bookSelfService.getBookSelf(UUID.fromString(uniqueId));
    }

    @PostMapping("/bookSelf/{uniqueId:[0-9a-f\\-]{36}}/add")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bookSelfAddNovel(@PathVariable("uniqueId") String uniqueId,
                                 @RequestBody @Valid Requests.BookSelfAddNovelRequest req,
                                 @CookieValue(name = "uid", defaultValue = "") String uid,
                                 @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                 @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                 HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();
        BookSelf bookSelf = bookSelfService.getBookSelf(UUID.fromString(uniqueId));
        if (!bookSelf.getAccountId().equals(currentAccount.getUid()))
            throw new FutureNovelException(FutureNovelException.Error.PERMISSION_DENIED, getMessage("shelf.can_not_edit_others"));
        NovelIndex novelIndex = novelService.getNovelIndex(UUID.fromString(req.novelIndexId));
        novelIndex.getChapters().removeAll();
        bookSelf.addNovel(objectMapper.valueToTree(novelIndex));
        bookSelfService.editBookSelf(bookSelf);
    }

    @PostMapping("/bookSelf/{uniqueId:[0-9a-f\\-]{36}}/remove")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bookSelfRemoveNovel(@PathVariable("uniqueId") String uniqueId,
                                    @RequestBody @Valid Requests.BookSelfAddNovelRequest req,
                                    @CookieValue(name = "uid", defaultValue = "") String uid,
                                    @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                    @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                    HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();
        BookSelf bookSelf = bookSelfService.getBookSelf(UUID.fromString(uniqueId));
        if (!bookSelf.getAccountId().equals(currentAccount.getUid()))
            throw new FutureNovelException(FutureNovelException.Error.PERMISSION_DENIED, getMessage("shelf.can_not_edit_others"));
        bookSelf.remove(UUID.fromString(req.novelIndexId));
        bookSelfService.editBookSelf(bookSelf);
    }

    @PostMapping("/bookSelf/{uniqueId:[0-9a-f\\-]{36}}/clear")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void bookSelfClear(@PathVariable("uniqueId") String uniqueId,
                              @CookieValue(name = "uid", defaultValue = "") String uid,
                              @CookieValue(name = "token", defaultValue = "") String tokenStr,
                              @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                              HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();
        BookSelf bookSelf = bookSelfService.getBookSelf(UUID.fromString(uniqueId));
        if (!bookSelf.getAccountId().equals(currentAccount.getUid()))
            throw new FutureNovelException(FutureNovelException.Error.PERMISSION_DENIED, getMessage("shelf.can_not_edit_others"));
        bookSelf.clear();
        bookSelfService.editBookSelf(bookSelf);
    }

    @GetMapping("/account/{accountId:[0-9a-f\\-]{36}}/bookSelves")
    @ResponseBody
    public ResponseEntity<List<BookSelf>> getBookSelves(@PathVariable("accountId") String accountId) {
        var result = bookSelfService.getBookSelves(UUID.fromString(accountId));
        if (result.isEmpty()) return ResponseEntity.noContent().build();
        else return ResponseEntity.ok(result);
    }

    @GetMapping("/novel/{novelIndexId:[0-9a-f\\-]{36}}/donate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void donate(@PathVariable("novelIndexId") String novelIndexId,
                       @RequestParam(defaultValue = "10") int count,
                       @CookieValue(name = "uid", defaultValue = "") String uid,
                       @CookieValue(name = "token", defaultValue = "") String tokenStr,
                       @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                       HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission();

        currentAccount.setExperience(currentAccount.getExperience().subtract(BigInteger.valueOf(count)));
        if (currentAccount.getExperience().compareTo(BigInteger.ZERO) < 0)
            throw new FutureNovelException(FutureNovelException.Error.EXP_NOT_ENOUGH);

        NovelIndex novelIndex = novelService.getNovelIndex(UUID.fromString(novelIndexId));
        if (novelIndex.getCopyright() == NovelIndex.Copyright.FAN_FICTION || novelIndex.getCopyright() == NovelIndex.Copyright.ORIGINAL) {
            Account author = accountService.getAccount(novelIndex.getUploader());
            author.setExperience(author.getExperience().add(BigInteger.valueOf(count)));
            accountService.updateAccountExperience(author);
        }
        accountService.updateAccountExperience(currentAccount);
    }

    @GetMapping("/statistics")
    @ResponseBody
    public Map<String, ?> statistics() {
        return Map.ofEntries(
            Map.entry("accounts", accountService.size()),
            Map.entry("novels", novelService.countAllNovelIndex()),
            Map.entry("timestamp", System.currentTimeMillis())
        );
    }

    @GetMapping("/clearGarbage")
    @ResponseBody
    public Map<String, Long> garbageCollect(@CookieValue(name = "uid", defaultValue = "") String uid,
                                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                            HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);
        return Map.ofEntries(
            Map.entry("cleared_comments", commentService.gc()),
            Map.entry("cleared_novel_nodes", novelService.gc()),
            Map.entry("cleared_read_histories", readHistoryService.gc()),
            Map.entry("timestamp", System.currentTimeMillis())
        );
    }

    @PostMapping("/settings/put")
    public ResponseEntity<JsonNode> putSetting(@Valid @RequestBody Requests.PutSettingRequest req,
                                               @CookieValue(name = "uid", defaultValue = "") String uid,
                                               @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                               @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                               HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);
        var old = settingService.put(req.key, req.value);
        if (old != null) return ResponseEntity.ok(old);
        else return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/settings/remove")
    public ResponseEntity<JsonNode> removeSetting(@RequestParam String key,
                                                  @CookieValue(name = "uid", defaultValue = "") String uid,
                                                  @CookieValue(name = "token", defaultValue = "") String tokenStr,
                                                  @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                                                  HttpServletRequest request) {
        Account currentAccount = checkLogin(uid, tokenStr, request.getRemoteAddr(), userAgent, true);
        currentAccount.checkPermission(Account.Permission.ADMIN);
        if (key.isBlank()) throw new IllegalArgumentException("key is empty");
        var old = settingService.remove(key, JsonNode.class);
        if (old != null) return ResponseEntity.ok(old);
        else return ResponseEntity.noContent().build();
    }

    @GetMapping("/settings/get")
    public ResponseEntity<JsonNode> getSetting(@RequestParam String key) {
        if (key.isBlank()) throw new IllegalArgumentException("key is empty");
        var value = settingService.get(key, JsonNode.class);
        if (value != null) return ResponseEntity.ok(value);
        else return ResponseEntity.noContent().build();
    }

    /**
     * 请求注册验证码
     *
     * @param session Session 服务端变量
     * @param req     请求参数，包含 email 字段
     */
    @PostMapping(value = "/sendCaptcha", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendCaptcha(HttpServletRequest request, HttpSession session, @Valid @RequestBody Requests.SendCaptchaRequest req) {
        String activateCode;
        var random = new Random();
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        activateCode = stringBuilder.toString();
        try {
            emailService.sendEmail(req.email, getMessage("email.email_verify.title", getMessage("home.title")),
                getMessage("email.email_verify",
                    serverUrl == null ? serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .build()
                        .normalize()
                        .toString() : serverUrl,
                    activateCode));
            session.setAttribute("activateCode", activateCode);
            session.setAttribute("activateBefore", System.currentTimeMillis() + Duration.ofMinutes(10).toMillis());
            session.setAttribute("activateEmail", req.email);
            DefaultFilter.blockIp(request.getRemoteAddr(), "/api/sendCaptcha", 30); // 30 秒后重试
        } catch (MailException | MessagingException e) {
            throw new FutureNovelException(e.getLocalizedMessage(), e);
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Responses.ErrorResponse> error(Exception e) {
        log.error("error: {}", e.getLocalizedMessage());
        var response = buildErrorResponse(e);
        return ResponseEntity.status(response.status)
            .body(response);
    }

}
