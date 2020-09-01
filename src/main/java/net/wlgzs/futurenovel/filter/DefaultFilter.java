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

package net.wlgzs.futurenovel.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.packet.Responses;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;

import static net.wlgzs.futurenovel.exception.FutureNovelException.Error.HACK_DETECTED;

/**
 * 过滤器，含有如下功能：<br />
 * 过滤掉请求过快的 IP 地址 <br />
 * 暂时禁止一个 IP 执行某项操作
 */
@Slf4j
@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST})
public class DefaultFilter extends HttpFilter {

    /**
     * 要监控请求速率限制的路径，单位：请求/分钟
     */
    private static final Map<String, Double> limitTable = new HashMap<>() {{
        put("/register", 2d);
        put("/api/sendCaptcha", 2d);
        put("/api/login", 12d);
        put("/search", 6d);
    }};

    /**
     * 采样数，至少为 2
     */
    private static final int COLLECT_COUNT = 5;

    /**
     * 保存每个 IP 地址对于每个监控路径的请求时间采样
     */
    private static final ConcurrentLinkedHashMap<String, HashMap<String, RequestInfo>> limitInfo = new ConcurrentLinkedHashMap.Builder<String, HashMap<String, RequestInfo>>()
        .maximumWeightedCapacity(500)
        .build();

    /**
     * 白名单
     */
    private static final List<String> whiteList = List.of(
        "127.0.0.1",
        "::1",
        "0:0:0:0:0:0:0:1"
    );

    /**
     * 暂时封禁一个 IP
     *
     * @param remoteAddr 要封禁的 IP
     * @param uri        对于哪一个路径（相对于 ContextPath)
     * @param time       封禁的时间，单位：秒
     */
    public static void blockIp(String remoteAddr, String uri, int time) {
        synchronized (DefaultFilter.class) {
            if (!whiteList.contains(remoteAddr) && limitTable.containsKey(uri)) {
                RequestInfo requestInfo = getRequestInfo(remoteAddr, uri);
                requestInfo.blockSeconds += time + 1;
            }
        }
    }

    public static void addLimitUrl(String url, double speed) {
        synchronized (DefaultFilter.class) {
            limitTable.put(url, speed);
        }
    }

    public static void removeLimitUrl(String url) {
        synchronized (DefaultFilter.class) {
            limitTable.remove(url);
        }
    }

    private static RequestInfo getRequestInfo(String remoteAddr, String uri) {
        HashMap<String, RequestInfo> infoMap = Optional.ofNullable(limitInfo.get(remoteAddr))
            .orElseGet(() -> {
                HashMap<String, RequestInfo> tmp = new HashMap<>(limitTable.size());
                tmp.put(uri, new RequestInfo());
                limitInfo.put(remoteAddr, tmp);
                return tmp;
            });
        return Optional.ofNullable(infoMap.get(uri))
            .orElseGet(() -> {
                var tmp = new RequestInfo();
                infoMap.put(uri, tmp);
                return tmp;
            });
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        var contextPath = req.getContextPath();
        var remoteAddr = req.getRemoteAddr();
        var uri = (contextPath.isBlank()) ? req.getRequestURI() : req.getRequestURI().replaceFirst(contextPath, "");
        synchronized (DefaultFilter.class) {
            boolean skip = (!uri.startsWith("/api") &&
                (req.getMethod().equalsIgnoreCase("GET") ||
                    req.getMethod().equalsIgnoreCase("HEAD"))) || // 跳过普通浏览请求
                req.getMethod().equalsIgnoreCase("OPTION"); // 跳过 OPTION 请求
            if (!skip && !whiteList.contains(remoteAddr) && limitTable.containsKey(uri)) {
                double limit = limitTable.get(uri);
                RequestInfo requestInfo = getRequestInfo(remoteAddr, uri);
                long[] tmpArr = new long[COLLECT_COUNT];
                System.arraycopy(requestInfo.lastRequestsTimeMillis, 1, tmpArr, 0, COLLECT_COUNT - 1);
                tmpArr[COLLECT_COUNT - 1] = System.currentTimeMillis();
                System.arraycopy(tmpArr, 0, requestInfo.lastRequestsTimeMillis, 0, COLLECT_COUNT);
                double rate = (double) 60 / ((double) (tmpArr[COLLECT_COUNT - 1] - tmpArr[0]) / (COLLECT_COUNT - 1) / 1000);
                if (rate > limit || (tmpArr[COLLECT_COUNT - 1] - tmpArr[COLLECT_COUNT - 2]) / 1000 < requestInfo.blockSeconds) {
                    log.warn("{} -- request too quickly! (path: {})", remoteAddr, uri);
                    Responses.ErrorResponse errResponse = new Responses.ErrorResponse();
                    errResponse.error = HACK_DETECTED.name();
                    errResponse.status = HACK_DETECTED.getStatusCode();
                    errResponse.errorMessage = errResponse.cause = "请求速率限制";
                    var context = RequestContextUtils.findWebApplicationContext(req);
                    MessageSource messageSource;
                    ObjectMapper objectMapper = null;
                    if (context != null) {
                        messageSource = context.getBean(MessageSource.class);
                        objectMapper = context.getBean(ObjectMapper.class);
                        errResponse.errorMessage = messageSource.getMessage(HACK_DETECTED.getErrorMessageCode(), null, HACK_DETECTED.getErrorMessageCode(), req.getLocale());
                    }
                    res.setStatus(errResponse.status);
                    res.setContentType("application/json; charset=utf-8");
                    ServletOutputStream output = res.getOutputStream();
                    if (output != null && objectMapper != null) {
                        output.write(objectMapper.writeValueAsBytes(errResponse));
                        output.flush();
                        output.close();
                    } else {
                        throw new FutureNovelException(HACK_DETECTED);
                    }
                    return;
                }
                if ((tmpArr[COLLECT_COUNT - 1] - tmpArr[COLLECT_COUNT - 2]) / 1000 > requestInfo.blockSeconds)
                    requestInfo.blockSeconds = -1;
            }
        }
        log.debug("req: {}", req.getRequestURL().toString());
        chain.doFilter(req, res);
    }

    @EqualsAndHashCode
    private static class RequestInfo {
        public final long[] lastRequestsTimeMillis = new long[COLLECT_COUNT];
        public int blockSeconds = -1;
    }

}
