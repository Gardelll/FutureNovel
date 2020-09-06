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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.wlgzs.futurenovel.AppProperties;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.Chapter;
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
import org.springframework.context.i18n.LocaleContextHolder;
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

    protected final ReadHistoryService readHistoryService;

    protected final BookSelfService bookSelfService;

    protected final CommentService commentService;

    protected final Validator defaultValidator;

    protected final AppProperties futureNovelConfig;

    protected final ObjectMapper objectMapper;

    protected final SettingService settingService;

    protected final MessageSource messageSource;

    protected String serverUrl = null;

    public AbstractAppController(TokenStore tokenStore,
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
        this.tokenStore = tokenStore;
        this.accountService = accountService;
        this.emailService = emailService;
        this.readHistoryService = readHistoryService;
        this.commentService = commentService;
        this.defaultValidator = defaultValidator;
        this.futureNovelConfig = futureNovelConfig;
        this.fileService = fileService;
        this.novelService = novelService;
        this.bookSelfService = bookSelfService;
        this.objectMapper = objectMapper;
        this.settingService = settingService;
        this.messageSource = messageSource;
    }

    /**
     * 从 Request 获取当前服务端的 URL 地址
     *
     * @return 服务器 URL 地址
     */
    public String getServerUrl() {
        return this.serverUrl == null ?
               this.serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                   .build()
                   .normalize()
                   .toString() :
               this.serverUrl;
    }

    protected String getBaseUri(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        if (contextPath.isBlank()) return "/";
        return contextPath;
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
     * 过滤富文本中的 javascript 代码
     *
     * @param html HTML 文本
     * @return 过滤后的 HTML 文本
     */
    protected String safeHTML(final String html) {
        return html.replaceAll("<script[^>]*", "<pre")
            .replaceAll("</script>", "</pre>")
            .replaceAll("on[a-zA-Z]+=", "event-data=");
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
     * @param uid       用户 ID
     * @param tokenStr  token 令牌
     * @param clientIp  用户 IP 地址
     * @param userAgent 浏览器 UA
     * @param session   Http Session
     * @return 登陆的用户
     */
    protected Account checkLoginAndSetSession(@NonNull String uid,
                                              @NonNull String tokenStr,
                                              @NonNull String clientIp,
                                              @NonNull String userAgent,
                                              @NonNull HttpSession session) {
        Account account = checkLogin(uid, tokenStr, clientIp, userAgent, false);
        session.setAttribute("currentAccount", account);
        return account;
    }

    protected String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * 获取当前请求的 URL（包含 GET 参数）
     *
     * @return 当前请求的 URL（包含 GET 参数）
     */
    public String getRequestUri() {
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().encode().build();
        var path = uri.getPath();
        var query = uri.getQuery();
        if (path == null) path = "/";
        if (query != null) return path + "?" + query;
        else return path;
    }

    public long countPage(long total, int perPage) {
        long pages = total / perPage;
        if (total % perPage != 0) pages++;
        return pages;
    }

    /**
     * 根据目录索引填充完整的目录信息
     *
     * @param novelIndexId 小说目录的 ID
     * @return 含有完整目录的小说对象
     */
    protected Responses.Novel buildNovel(@NonNull UUID novelIndexId) {
        // 1. 根据 ID 查询目录信息
        var novelIndex = novelService.getNovelIndex(novelIndexId);

        var novel = new Responses.Novel(novelIndex);
        var chapterIdList = new ArrayList<UUID>();
        var arrayNode = novelIndex.getChapters();
        for (JsonNode jsonNode : arrayNode) chapterIdList.add(UUID.fromString(jsonNode.asText()));
        ConcurrentHashMap<UUID, Responses.NovelChapter> chapterIndex = new ConcurrentHashMap<>(); // chapterId 到 NovelChapter 的映射
        ConcurrentHashMap<UUID, Chapter> chapterIndexTemp = new ConcurrentHashMap<>();

        // 2. 查询目录下的所有章节
        List<Chapter> chapterListTemp = novelService.findChapterByFromNovel(novelIndexId, 0, Integer.MAX_VALUE, null);

        // 整理一次
        for (Chapter chapter : chapterListTemp) {
            if (!chapterIdList.contains(chapter.getThisUUID())) chapterIdList.add(chapter.getThisUUID());
            chapterIndexTemp.put(chapter.getThisUUID(), chapter);
            chapterIndex.put(chapter.getThisUUID(), new Responses.NovelChapter(chapter));
        }

        // 3. 一次性获取目录下的所有小节（不含内容）
        var sectionInfoList = novelService.getSectionInfoByFromChapterList(chapterIdList);

        // 根据映射添加到章节里面
        for (Responses.NovelChapter.SectionInfo sectionInfo : sectionInfoList) {
            Optional.ofNullable(chapterIndex.get(sectionInfo.getFromChapter()))
                .orElseThrow(() -> new FutureNovelException(FutureNovelException.Error.NOVEL_NOT_FOUND))
                .add(sectionInfo);
        }

        // 按照目录标题排序
        var chapterList = chapterIndex.values();
        for (Responses.NovelChapter chapter : chapterList) {
            var arrayNode2 = chapterIndexTemp.get(chapter.getUniqueId()).getSections();
            final ArrayList<UUID> orderList = new ArrayList<>();
            for (JsonNode jsonNode : arrayNode2) orderList.add(UUID.fromString(jsonNode.asText()));
            chapter.sort(Comparator.comparingInt(o -> orderList.indexOf(o.getThisUUID())));
        }

        // 生成最终结果
        novel.addAll(chapterList);
        novel.sort(Comparator.comparingInt(o -> chapterIdList.indexOf(o.getThisUUID())));
        return novel;
    }

    protected Responses.ErrorResponse buildErrorResponse(Exception e) {
        Responses.ErrorResponse response = new Responses.ErrorResponse();
        if (e instanceof HttpMessageNotReadableException ||
            e instanceof HttpMessageNotWritableException ||
            e instanceof IllegalArgumentException ||
            e instanceof ServletRequestBindingException) {
            var errorMessage = e.getLocalizedMessage();
            if (errorMessage == null || errorMessage.isBlank())
                errorMessage = getMessage("error.illegal_argument");
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
            response.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return response;
    }

    public void handle(HttpServletRequest request) {
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        var errorStatus = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        var ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        int status = errorStatus == null ? 501 : errorStatus;
        if (errorMessage != null && !errorMessage.isBlank())
            throw new ResponseStatusException(HttpStatus.valueOf(status), errorMessage, ex);
        else throw new ResponseStatusException(HttpStatus.valueOf(status), null, ex);
    }

}
