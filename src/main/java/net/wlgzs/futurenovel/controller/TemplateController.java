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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.AppProperties;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
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
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
public class TemplateController extends AbstractAppController implements ErrorController {

    public TemplateController(TokenStore tokenStore,
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
     * 主页路由
     *
     * @param uid       Cookie：用户 ID
     * @param tokenStr  Cookie：登陆令牌
     * @param userAgent Header：浏览器标识
     * @param request   Http 请求
     * @param session   Http Session
     * @param model     模板属性
     * @return 对应的视图
     */
    @GetMapping({"", "/", "/index"})
    public String home(@CookieValue(name = "uid", defaultValue = "") String uid,
                       @CookieValue(name = "token", defaultValue = "") String tokenStr,
                       @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                       HttpServletRequest request,
                       HttpSession session,
                       Model model) {
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);

        List<NovelIndex> random = novelService.getAllNovelIndex(0, 20, Requests.SearchNovelRequest.SortBy.RANDOM.getOrderBy());
        model.addAttribute("suggestNovelIndexList", random);

        List<NovelIndex> hot = novelService.getAllNovelIndex(0, 20, Requests.SearchNovelRequest.SortBy.HOT_DESC.getOrderBy());
        model.addAttribute("hotNovelIndexList", hot);

        List<NovelIndex> newest = novelService.getAllNovelIndex(0, 20, Requests.SearchNovelRequest.SortBy.CREATE_TIME_DESC.getOrderBy());
        model.addAttribute("newestNovelIndexList", newest);

        List<NovelIndex> all = new LinkedList<>(new LinkedHashSet<>() {{
            addAll(random);
            addAll(hot);
            addAll(newest);
        }});

        all.sort((o1, o2) -> Long.compare(o2.getHot(), o1.getHot()));

        var covers = settingService.get("covers", List.class);
        if (covers != null) {
            model.addAttribute("covers", covers);
        } else {
            HashSet<String> covers2 = all.stream()
                .filter(novelIndex -> novelIndex.getCoverImgUrl() != null)
                .map(NovelIndex::getCoverImgUrl).collect(HashSet::new, HashSet::add, AbstractCollection::addAll);
            model.addAttribute("covers", List.copyOf(covers2).subList(0, Math.min(10, covers2.size())));
        }

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
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        m.addAttribute("errorMessage", "OK");
        if (!redirectTo.isBlank()) {
            session.setAttribute("redirectTo", redirectTo);
        }
        var tags = novelService.getTags();
        var series = novelService.getSeries();

        m.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        m.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));
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
        var tags = novelService.getTags();
        var series = novelService.getSeries();

        model.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        model.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));
        return "login";
    }

    /**
     * 注册表单路由
     *
     * @param req      请求参数，详见 {@link Requests.RegisterRequest}
     * @param m        模板属性
     * @param request  Http 请求
     * @param response Http 响应
     * @param session  Session 服务端变量
     * @return 含有错误信息的视图或跳转响应
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String register(@Valid Requests.RegisterRequest req,
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
                throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("register.different_password"));
            if (accountService.isEmailUsed(req.email, null))
                throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, getMessage("register.email_already_in_use", req.email));
            if (accountService.isEmailUsed(req.userName, null))
                throw new FutureNovelException(FutureNovelException.Error.USER_EXIST, getMessage("register.username_already_in_use", req.userName));
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
                uidCookie.setPath(getBaseUri(request));
                response.addCookie(uidCookie);
                session.setAttribute("activateCode", null);
                session.setAttribute("activateBefore", null);
                session.setAttribute("activateEmail", null);
                m.addAttribute("errorMessage", getMessage("register.success"));
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
        var tags = novelService.getTags();
        var series = novelService.getSeries();

        m.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        m.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));
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
        checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        var novel = buildNovel(UUID.fromString(uniqueId));
        novelService.hotAddOne(novel.getUniqueId());

        model.addAttribute("novel", novel);
        var tags = novelService.getTags();
        var series = novelService.getSeries();

        model.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        model.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));
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
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);

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
            .filter(c -> c.getUniqueId().equals(section.getFromChapter()))
            .findFirst()
            .orElseGet(() -> new Responses.NovelChapter(novelService.getChapter(section.getFromChapter())));
        model.addAttribute("chapter", chapter);
        chapter.stream().filter(sectionInfo -> sectionInfo.getUniqueId().equals(section.getUniqueId())).findFirst().ifPresent(sectionInfo -> {
            int index = chapter.indexOf(sectionInfo);
            if (index + 1 < chapter.size()) {
                model.addAttribute("nextSection", chapter.get(index + 1));
            } else {
                int index2 = novel.indexOf(chapter);
                while (index2 + 1 < novel.size()) {
                    var nextChapter = novel.get(index2 + 1);
                    if (!nextChapter.isEmpty()) {
                        model.addAttribute("nextSection", nextChapter.getSections().peek());
                        break;
                    } else index2++;
                }
            }
            if (index > 0) {
                model.addAttribute("previewSection", chapter.get(index - 1));
            } else {
                int index2 = novel.indexOf(chapter);
                while (index2 > 1) {
                    var preChapter = novel.get(index2 - 1);
                    if (!preChapter.isEmpty()) {
                        model.addAttribute("previewSection", preChapter.getSections().peekLast());
                        break;
                    } else index2--;
                }
            }
        });
        var tags = novelService.getTags();
        var series = novelService.getSeries();

        model.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        model.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));

        return "read-book";
    }

    @GetMapping({"/user"})
    public String userCenterRedirect(@RequestParam(value = "userName", defaultValue = "") String userName,
                             @CookieValue(name = "uid", defaultValue = "") String uid,
                             HttpServletRequest request,
                             Model model) {
        if (!userName.isBlank()) {
            try {
                var account = accountService.getAccount(userName);
                return String.format("redirect:/user/%s", account.getUid().toString());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
            }
        }
        if (!uid.matches("^[0-9a-f\\-]{36}$")) {
            model.addAttribute("errorMessage", getMessage("login.please_login"));
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        return String.format("redirect:/user/%s", uid);
    }

    @GetMapping({"/user/{uuid:[0-9a-f\\-]{36}}"})
    public String userCenter(@PathVariable(required = false) String uuid,
                             @CookieValue(name = "uid", defaultValue = "") String uid,
                             @CookieValue(name = "token", defaultValue = "") String tokenStr,
                             @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                             HttpServletRequest request,
                             HttpSession session,
                             Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        if (uuid == null) uuid = uid;
        try {
            Account showAccount = accountService.getAccount(UUID.fromString(uuid));
            model.addAttribute("showAccount", showAccount);
            if (currentAccount != null && showAccount.getUid().equals(currentAccount.getUid())) {
                List<ReadHistory> history = readHistoryService.getReadHistory(currentAccount, new Date(System.currentTimeMillis() - Duration.ofDays(14).toMillis()), null);
                model.addAttribute("readHistory", history);
            }
            var tags = novelService.getTags();
            var series = novelService.getSeries();

            model.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
            model.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));
            return "member";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getLocalizedMessage(), e);
        }
    }

    @GetMapping(value = "/search", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String hotSearch(HttpServletRequest request) {
        return
            "<!DOCTYPE html>\n" +
            "<html lang=\"zh\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Redirecting</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<form id=\"myForm\" action=\"" + getBaseUri(request) + "search?page=1\" method=\"post\">\n" +
            "    <input type=\"hidden\" name=\"searchBy\" value=\"HOT\">\n" +
            "    <input type=\"hidden\" name=\"sortBy\" value=\"HOT_DESC\">\n" +
            "</form>\n" +
            "<script type=\"text/javascript\">\n" +
            "    document.getElementById(\"myForm\").submit()\n" +
            "</script>\n" +
            "</body>\n" +
            "</html>\n";
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String doSearch(@Valid Requests.SearchNovelRequest req,
                           @RequestParam(name = "page") int page,
                           @RequestParam(name = "per_page", defaultValue = "20") int perPage,
                           @CookieValue(name = "uid", defaultValue = "") String uid,
                           @CookieValue(name = "token", defaultValue = "") String tokenStr,
                           @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                           HttpServletRequest request,
                           HttpSession session,
                           Model model) {
        if (page <= 0 || perPage <= 0 || perPage > 100)
            throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT);
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        model.addAttribute("searchBy", req.searchBy);
        model.addAttribute("sortBy", req.sortBy);
        model.addAttribute("keywords", req.keywords);
        model.addAttribute("except", req.except);
        model.addAttribute("after", req.after);
        model.addAttribute("before", req.before);
        model.addAttribute("page", page);
        int offset = (page - 1) * perPage;
        switch (req.searchBy) {
            case HOT: {
                long total = novelService.countAllNovelIndex();
                List<NovelIndex> result = novelService.getAllNovelIndex(offset, perPage, Requests.SearchNovelRequest.SortBy.HOT_DESC.getOrderBy());
                model.addAttribute("totalPage", (total / perPage + (total >= perPage ? 0 : 1)));
                model.addAttribute("novelIndexList", result);
                break;
            }
            case KEYWORDS: {
                if (req.keywords == null)
                    throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("search.empty_keywords"));
                String except = req.except == null ? "" :
                                Arrays.stream(req.except.split("\\s+"))
                                    .map(s -> String.format("~\"%s\"", s.replaceAll("\"", "\\")))
                                    .collect(Collectors.joining(" "));
                String query = String.format("%s %s", Arrays.stream(req.keywords.split("\\s+"))
                    .map(s -> String.format("\"%s\"", s.replaceAll("\"", "\\")))
                    .collect(Collectors.joining(" ")), except);
                log.info("query = {}", query);
                long total = novelService.searchNovelIndexGetCount(query);
                List<NovelIndex> result = novelService.searchNovelIndex(query, offset, perPage, req.sortBy.getOrderBy());
                model.addAttribute("totalPage", (total / perPage + (total >= perPage ? 0 : 1)));
                model.addAttribute("novelIndexList", result);
                break;
            }
            case CONTENT: {
                if (currentAccount == null) {
                    model.asMap().clear();
                    model.addAttribute("errorMessage", getMessage("login.please_login"));
                    model.addAttribute("redirectTo", request.getRequestURI());
                    return "redirect:/login";
                }
                if (!currentAccount.isVIP())
                    currentAccount.setExperience(currentAccount.getExperience().subtract(BigInteger.ONE));
                if (currentAccount.getExperience().compareTo(BigInteger.ZERO) < 0)
                    throw new FutureNovelException(FutureNovelException.Error.EXP_NOT_ENOUGH);
                if (req.keywords == null || req.keywords.length() < 5)
                    throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("search.keywords_are_too_short"));
                String except = req.except == null ? "" :
                                Arrays.stream(req.except.split("\\s+"))
                                    .map(s -> String.format("~\"%s\"", s.replaceAll("\"", "\\")))
                                    .collect(Collectors.joining(" "));
                var keywords = req.keywords.split("\\s+");
                String query = String.format("%s %s", Arrays.stream(keywords)
                    .map(s -> String.format("\"%s\"", s.replaceAll("\"", "\\")))
                    .collect(Collectors.joining(" ")), except);
                long total = novelService.searchByContentGetCount(query);
                List<Section> result = novelService.searchByContent(query, offset, perPage);
                // 关键词着色
                for (Section section : result) {
                    // 删除 HTML 标记，只保留文本
                    String text = section.getText().replaceAll("<style[^>]*>[\\s\\S]*?</style>", "").replaceAll("<[^>]+>", " ").replaceAll("[\\s\r\n\t]+", " ");
                    var queue = new LinkedList<Integer>();
                    for (String word : keywords) {
                        var matcher = Pattern.compile(word, Pattern.LITERAL | Pattern.CASE_INSENSITIVE).matcher(text);
                        while (matcher.find()) {
                            int a = matcher.start();
                            // 若此着色点距离上一个不远（25 字以内），那就合并一下
                            // 虽然这样会把中间的字符也染色，但问题不大
                            if (!queue.isEmpty() && (a - queue.peekLast() < 25)) {
                                queue.removeLast();
                            } else {
                                queue.addLast(a);
                            }
                            queue.addLast(matcher.end());
                        }
                    }
                    var sb = new StringBuilder();
                    while (!queue.isEmpty()) {
                        int a = queue.poll();
                        int b = queue.peek() == null ? text.length() : queue.poll();
                        // 若第一个着色点不是在开头，则加上省略号
                        if (sb.length() == 0 && a > 0) sb.append("……");
                        sb.append(text, Math.max(0, a - 25), a) // 向前延申 25 字
                            .append("<span style='color: red'>")
                            .append(text, a, b) // 截取关键词染成红色
                            .append("</span>")
                            .append(text, b, Math.min(b + 25, text.length())); // 向后拓展 25 字
                        if (b + 25 < text.length()) sb.append("……"); // 如果后面还有内容，则加上省略号
                    }
                    section.setText(sb.toString());
                }
                if (!result.isEmpty()) {
                    accountService.updateAccountExperience(currentAccount);
                }
                model.addAttribute("totalPage", (total / perPage + (total >= perPage ? 0 : 1)));
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
                        if (chapterIds.contains(u)) chapterIdToNovelIndexMap.put(u, novelIndex);
                    }
                }
                model.addAttribute("chapterIdToNovelIndexMap", chapterIdToNovelIndexMap);
                break;
            }
            case PUBDATE: {
                if (req.after == null || req.before == null)
                    throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, getMessage("search.empty_time_duration"));
                long total = novelService.countNovelIndexByPubDate(req.after, req.before);
                List<NovelIndex> result = novelService.findNovelIndexByPubDate(req.after, req.before, offset, perPage, req.sortBy.getOrderBy());
                model.addAttribute("totalPage", (total / perPage + (total >= perPage ? 0 : 1)));
                model.addAttribute("novelIndexList", result);
                break;
            }
        }
        var tags = novelService.getTags();
        var series = novelService.getSeries();

        model.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        model.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));
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
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", getMessage("login.please_login"));
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        var novel = buildNovel(UUID.fromString(uniqueId));

        model.addAttribute("novel", novel);
        var tags = novelService.getTags();
        var series = novelService.getSeries();

        model.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        model.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));
        return "writer";
    }

    @GetMapping("/novel/create")
    public String novelCreate(@CookieValue(name = "uid", defaultValue = "") String uid,
                              @CookieValue(name = "token", defaultValue = "") String tokenStr,
                              @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                              HttpServletRequest request,
                              HttpSession session,
                              Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", getMessage("login.please_login"));
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        var tags = novelService.getTags();
        var series = novelService.getSeries();

        model.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        model.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));
        return "WorkInformation";
    }

    @GetMapping({"/admin/", "/admin", "/admin/index"})
    public String adminHome(@CookieValue(name = "uid", defaultValue = "") String uid,
                            @CookieValue(name = "token", defaultValue = "") String tokenStr,
                            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
                            HttpServletRequest request,
                            HttpSession session,
                            Model model) {
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", getMessage("login.please_login"));
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
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", getMessage("login.please_login"));
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
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", getMessage("login.please_login"));
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
        Account currentAccount = checkLoginAndSetSession(uid, tokenStr, request.getRemoteAddr(), userAgent, session);
        if (currentAccount == null) {
            model.addAttribute("errorMessage", getMessage("login.please_login"));
            model.addAttribute("redirectTo", request.getRequestURI());
            return "redirect:/login";
        }
        currentAccount.checkPermission(Account.Permission.ADMIN);
        return "backstage-comment";
    }

    @ExceptionHandler(Exception.class)
    public String error(Model model, HttpServletResponse response, Exception e) {
        log.error("error: {}", e.getLocalizedMessage());
        Responses.ErrorResponse responseErr = buildErrorResponse(e);
        model.addAttribute("status", responseErr.status);
        model.addAttribute("error", responseErr.error);
        model.addAttribute("cause", responseErr.cause);
        model.addAttribute("errorMessage", responseErr.errorMessage);
        var tags = novelService.getTags();
        var series = novelService.getSeries();

        model.addAttribute("tags", List.copyOf(tags).subList(0, Math.min(14, tags.size())));
        model.addAttribute("series", List.copyOf(series).subList(0, Math.min(20, series.size())));
        response.setStatus(responseErr.status);
        return "error";
    }

    @Override
    @RequestMapping(path = "/error")
    public void handle(HttpServletRequest request) {
        super.handle(request);
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

}
