package net.wlgzs.futurenovel.controller;

import java.math.BigInteger;
import java.time.Duration;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.NovelIndex;
import net.wlgzs.futurenovel.model.ReadHistory;
import net.wlgzs.futurenovel.model.Section;
import net.wlgzs.futurenovel.packet.c2s.RegisterRequest;
import net.wlgzs.futurenovel.packet.c2s.SearchNovelRequest;
import net.wlgzs.futurenovel.packet.s2c.ErrorResponse;
import net.wlgzs.futurenovel.packet.s2c.NovelChapter;
import net.wlgzs.futurenovel.service.AccountService;
import net.wlgzs.futurenovel.service.BookSelfService;
import net.wlgzs.futurenovel.service.CommentService;
import net.wlgzs.futurenovel.service.EmailService;
import net.wlgzs.futurenovel.service.FileService;
import net.wlgzs.futurenovel.service.NovelService;
import net.wlgzs.futurenovel.service.ReadHistoryService;
import net.wlgzs.futurenovel.service.TokenStore;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

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
                              ReadHistoryService readHistoryService,
                              CommentService commentService,
                              Validator defaultValidator,
                              Properties futureNovelConfig,
                              FileService fileService,
                              BookSelfService bookSelfService,
                              DateFormatter defaultDateFormatter) {
        super(tokenStore, accountService, emailService, novelService, readHistoryService, commentService, defaultValidator, futureNovelConfig, fileService, bookSelfService, defaultDateFormatter);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
       super.initBinder(binder);
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

        List<NovelIndex> random = novelService.getAllNovelIndex(0, 12, SearchNovelRequest.SortBy.RANDOM.getOrderBy());
        model.addAttribute("suggestNovelIndexList", random);

        List<NovelIndex> hot = novelService.getAllNovelIndex(0, 12, SearchNovelRequest.SortBy.HOT_DESC.getOrderBy());
        model.addAttribute("hotNovelIndexList", hot);

        List<NovelIndex> newest = novelService.getAllNovelIndex(0, 10, SearchNovelRequest.SortBy.CREATE_TIME_DESC.getOrderBy());
        model.addAttribute("newestNovelIndexList", newest);

        List<NovelIndex> all = new LinkedList<>(new LinkedHashSet<>() {{
            addAll(random);
            addAll(hot);
            addAll(newest);
        }});

        all.sort((o1, o2) -> Long.compare(o2.getHot(), o1.getHot()));

        HashSet<String> covers = all.stream()
            .filter(novelIndex -> novelIndex.getCoverImgUrl() != null)
            .map(NovelIndex::getCoverImgUrl).collect(HashSet::new, HashSet::add, AbstractCollection::addAll);
        model.addAttribute("covers", List.copyOf(covers).subList(0, Math.min(10, covers.size())));

        var tags = novelService.getTags();
        var series = novelService.getSeries();

        model.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        model.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));

        return "FutureNovel";
    }

    @GetMapping("/register")
    public String register(Model m, @RequestParam(name = "redirectTo", defaultValue = "") String redirectTo,
                           @CookieValue(name = "uid", defaultValue = "") String uid,
                           @CookieValue(name = "token", defaultValue = "") String tokenStr,
                           @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                           HttpServletRequest request,
                           HttpSession session) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        m.addAttribute("errorMessage", "OK");
        if (!redirectTo.isBlank()) {
            session.setAttribute("redirectTo", redirectTo);
        }
        return "register";
    }

    @GetMapping({"/login"})
    public String login(@RequestParam(name = "redirectTo", defaultValue = "") String redirectTo,
                        @RequestParam(name = "errorMessage", defaultValue = "OK") String errorMessage,
                        Model model,
                        HttpSession session) {
        if (!redirectTo.isBlank()) {
            session.setAttribute("redirectTo", redirectTo);
        }
        model.addAttribute("errorMessage", errorMessage);
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
                    BigInteger.ZERO,
                    null);
                accountService.register(account);
                var uidCookie = new Cookie("uid", account.getUid().toString());
                uidCookie.setMaxAge((int) Duration.ofDays(365).toSeconds());
                uidCookie.setPath(request.getContextPath());
                response.addCookie(uidCookie);
                session.setAttribute("activateCode", null);
                session.setAttribute("activateBefore", null);
                session.setAttribute("activateEmail", null);
                m.addAttribute("errorMessage", "注册成功，请登录");
                if (req.redirectTo != null && !req.redirectTo.isBlank()) {
                    session.setAttribute("redirectTo", req.redirectTo);
                    m.addAttribute("redirectTo", req.redirectTo);
                }
                return "redirect:/login";
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

    @GetMapping("/novel/{uniqueId:[0-9a-f\\-]{36}}/view")
    public String bookView(@PathVariable(name = "uniqueId") String uniqueId,
                           @CookieValue(name = "uid", defaultValue = "") String uid,
                           @CookieValue(name = "token", defaultValue = "") String tokenStr,
                           @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                           HttpServletRequest request,
                           HttpSession session,
                           Model model) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        var novel = buildNovel(UUID.fromString(uniqueId));
        novelService.hotAddOne(novel.getUniqueId());

        model.addAttribute("novel", novel);
        return "look-book";
    }

    @GetMapping("/novel/read")
    public String bookRead(@RequestParam("sectionId") String sectionIdStr, Model model) {
        final UUID sectionId = UUID.fromString(sectionIdStr);
        final var novelIndex = novelService.findNovelIndexBySectionId(sectionId);
        model.addAttribute("sectionId", sectionIdStr);
        return String.format("redirect:/novel/%s/read", novelIndex.getUniqueId().toString());
    }

    @GetMapping("/novel/{uniqueId:[0-9a-f\\-]{36}}/read")
    public String bookRead(@PathVariable(name = "uniqueId") String uniqueId,
                           @RequestParam("sectionId") String sectionId,
                           @CookieValue(name = "uid", defaultValue = "") String uid,
                           @CookieValue(name = "token", defaultValue = "") String tokenStr,
                           @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                           HttpServletRequest request,
                           HttpSession session,
                           Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);

        var novel = buildNovel(UUID.fromString(uniqueId));
        model.addAttribute("novel", novel);
        novelService.hotAddOne(novel.getUniqueId());

        var section = novelService.getSection(UUID.fromString(sectionId));
        model.addAttribute("section", section);

        if (currentAccount != null) {
            readHistoryService.recordReadHistory(currentAccount, section);
        }

        var chapter = novel.getChapters()
            .stream()
            .filter( c -> c.getUniqueId().equals(section.getFromChapter()))
            .findFirst()
            .orElseGet(() -> new NovelChapter(novelService.getChapter(section.getFromChapter())));
        model.addAttribute("chapter", chapter);

        return "read-book";
    }

    @GetMapping({"/user/{uuid:[0-9a-f\\-]{36}}"})
    public String userCenter(@PathVariable(required = false) String uuid,
                             @CookieValue(name = "uid", defaultValue = "") String uid,
                             @CookieValue(name = "token", defaultValue = "") String tokenStr,
                             @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                             HttpServletRequest request,
                             HttpSession session,
                             Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        if (uuid == null) uuid = uid;
        try {
            Account showAccount = accountService.getAccount(UUID.fromString(uuid));
            model.addAttribute("showAccount", showAccount);
            if (currentAccount.getUid().equals(showAccount.getUid())) {
                List<ReadHistory> history = readHistoryService.getReadHistory(currentAccount, new Date(System.currentTimeMillis() - Duration.ofDays(14).toMillis()), null);
                model.addAttribute("readHistory", history);
            }
            return "member";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
        }
    }

    @GetMapping("/search")
    public String search(@CookieValue(name = "uid", defaultValue = "") String uid,
                         @CookieValue(name = "token", defaultValue = "") String tokenStr,
                         @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                         HttpServletRequest request,
                         HttpSession session) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        return "search";
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String doSearch(@Valid SearchNovelRequest req,
                           @RequestParam(name = "page") int page,
                           @RequestParam(name = "per_page", defaultValue = "20") int perPage,
                           @CookieValue(name = "uid", defaultValue = "") String uid,
                           @CookieValue(name = "token", defaultValue = "") String tokenStr,
                           @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                           HttpServletRequest request,
                           HttpSession session,
                           Model model) {
        if (page <= 0 || perPage <= 0 || perPage > 100) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        model.addAttribute("searchBy", req.searchBy);
        model.addAttribute("sortBy", req.sortBy);
        int offset = (page - 1) * perPage;
        switch (req.searchBy) {
            case HOT: {
                long total = novelService.countAllNovelIndex();
                List<NovelIndex> result = novelService.getAllNovelIndex(offset, perPage, SearchNovelRequest.SortBy.HOT_DESC.getOrderBy());
                model.addAttribute("totalPage", total / perPage + 1);
                model.addAttribute("novelIndexList", result);
                break;
            }
            case KEYWORDS: {
                if (req.keywords == null) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, "关键字为空");
                String except = req.except == null ? "" : Arrays.stream(req.except.split("\\s+")).map(s -> "-" + s).collect(Collectors.joining(" "));
                String query = Arrays.stream(req.keywords.split("\\s+")).map(s -> "+" + s).collect(Collectors.joining(" ")) + " " + except;
                long total = novelService.searchNovelIndexGetCount(query);
                List<NovelIndex> result = novelService.searchNovelIndex(query, offset, perPage, req.sortBy.getOrderBy());
                model.addAttribute("totalPage", total / perPage + 1);
                model.addAttribute("novelIndexList", result);
                break;
            }
            case CONTENT: {
                if (currentAccount == null) {
                    model.asMap().clear();
                    model.addAttribute("errorMessage", "请先登录");
                    model.addAttribute("redirectTo", request.getRequestURI());
                    return "redirect:/login";
                }
                if (!currentAccount.isVIP())
                    currentAccount.setExperience(currentAccount.getExperience().subtract(BigInteger.ONE));
                if (currentAccount.getExperience().compareTo(BigInteger.ZERO) < 0) throw new FutureNovelException(FutureNovelException.Error.EXP_NOT_ENOUGH);
                if (req.keywords == null || req.keywords.length() < 5) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, "关键字太短");
                String except = req.except == null ? "" : Arrays.stream(req.except.split("\\s+")).map(s -> "-" + s).collect(Collectors.joining(" "));
                String query = Arrays.stream(req.keywords.split("\\s+")).map(s -> "+" + s).collect(Collectors.joining(" ")) + " " + except;
                long total = novelService.searchByContentGetCount(query);
                // TODO 关键词着色
                List<Section> result = novelService.searchByContent(query, offset, perPage);
                if (!result.isEmpty()) {
                    accountService.updateAccountExperience(currentAccount);
                }
                model.addAttribute("totalPage", total / perPage + 1);
                model.addAttribute("sectionList", result);
                List<UUID> chapterIds = result.stream()
                    .map(Section::getFromChapter)
                    .collect(Collectors.toList());
                List<NovelIndex> novelIndexList = novelService.findNovelIndexByChapterIdList(chapterIds);
                ConcurrentHashMap<UUID, NovelIndex> chapterIdToNovelIndexMap = new ConcurrentHashMap<>(5);
                for (NovelIndex novelIndex : novelIndexList) {
                    HashSet<UUID> containedChapterIds = new HashSet<>();
                    novelIndex.getChapters().forEach(jsonNode -> containedChapterIds.add(UUID.fromString(jsonNode.asText())));
                    for (UUID u : containedChapterIds) {
                        if(chapterIds.contains(u)) chapterIdToNovelIndexMap.put(u, novelIndex);
                    }
                }
                model.addAttribute("chapterIdToNovelIndexMap", chapterIdToNovelIndexMap);
                break;
            }
            case PUBDATE: {
                if (req.after == null || req.before == null)
                    throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, "时间段为空");
                long total = novelService.countNovelIndexByPubDate(req.after, req.before);
                List<NovelIndex> result = novelService.findNovelIndexByPubDate(req.after, req.before, offset, perPage, req.sortBy.getOrderBy());
                model.addAttribute("totalPage", total / perPage + 1);
                model.addAttribute("novelIndexList", result);
                break;
            }
        }
        return "search";
    }

    @GetMapping("/novel/{uniqueId:[0-9a-f\\-]{36}}/write")
    public String novelWrite(@PathVariable("uniqueId") String uniqueId,
                             @CookieValue(name = "uid", defaultValue = "") String uid,
                             @CookieValue(name = "token", defaultValue = "") String tokenStr,
                             @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                             HttpServletRequest request,
                             HttpSession session,
                             Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", "请先登录");
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        var novel = buildNovel(UUID.fromString(uniqueId));

        model.addAttribute("novel", novel);
        return "writer";
    }

    @GetMapping("/novel/create")
    public String novelCreate(@CookieValue(name = "uid", defaultValue = "") String uid,
                              @CookieValue(name = "token", defaultValue = "") String tokenStr,
                              @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                              HttpServletRequest request,
                              HttpSession session,
                              Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", "请先登录");
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        return "WorkInformation";
    }

    @GetMapping({"/admin/", "/admin", "/admin/index"})
    public String adminHome(@CookieValue(name = "uid", defaultValue = "") String uid,
                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                            HttpServletRequest request,
                            HttpSession session,
                            Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", "请先登录");
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        currentAccount.checkPermission(Account.Permission.ADMIN);
        return "index";
    }

    @GetMapping({"/admin/books"})
    public String novelAdmin(@CookieValue(name = "uid", defaultValue = "") String uid,
                             @CookieValue(name = "token", defaultValue = "") String tokenStr,
                             @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                             HttpServletRequest request,
                             HttpSession session,
                             Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", "请先登录");
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        currentAccount.checkPermission(Account.Permission.ADMIN);
        return "backstage-book";
    }

    @GetMapping({"/admin/users"})
    public String userAdmin(@CookieValue(name = "uid", defaultValue = "") String uid,
                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                            HttpServletRequest request,
                            HttpSession session,
                            Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", "请先登录");
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        currentAccount.checkPermission(Account.Permission.ADMIN);
        return "backstage-user";
    }

    @GetMapping({"/admin/comments"})
    public String commentAdmin(@CookieValue(name = "uid", defaultValue = "") String uid,
                               @CookieValue(name = "token", defaultValue = "") String tokenStr,
                               @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                               HttpServletRequest request,
                               HttpSession session,
                               Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session, false);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", "请先登录");
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        currentAccount.checkPermission(Account.Permission.ADMIN);
        return "backstage-comment";
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
