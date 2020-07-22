package net.wlgzs.futurenovel.filter;

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
import net.wlgzs.futurenovel.AppConfig;
import net.wlgzs.futurenovel.bean.ErrorResponse;
import net.wlgzs.futurenovel.exception.FutureNovelException;

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
    private static final Map<String, Integer> limitTable = Map.ofEntries(
            Map.entry("/register", 2),
            Map.entry("/api/sendCaptcha", 2),
            Map.entry("/api/login", 12)
    );

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
            if (!whiteList.contains(remoteAddr) && limitTable.containsKey(uri)) {
                int limit = limitTable.get(uri);
                RequestInfo requestInfo = getRequestInfo(remoteAddr, uri);
                long[] tmpArr = new long[COLLECT_COUNT];
                System.arraycopy(requestInfo.lastRequestsTimeMillis, 1, tmpArr, 0, COLLECT_COUNT - 1);
                tmpArr[COLLECT_COUNT - 1] = System.currentTimeMillis();
                System.arraycopy(tmpArr, 0, requestInfo.lastRequestsTimeMillis, 0, COLLECT_COUNT);
                double rate = (double) 60 / ((double) (tmpArr[COLLECT_COUNT - 1] - tmpArr[0]) / (COLLECT_COUNT - 1) / 1000);
                if (rate > limit || (tmpArr[COLLECT_COUNT - 1] - tmpArr[COLLECT_COUNT - 2]) / 1000 < requestInfo.blockSeconds) {
                    log.warn("{} -- request too quickly! (path: {})", remoteAddr, uri);
                    ErrorResponse errResponse = new ErrorResponse();
                    errResponse.error = FutureNovelException.Error.HACK_DETECTED.name();
                    errResponse.errorMessage = FutureNovelException.Error.HACK_DETECTED.getErrorMessage();
                    errResponse.status = FutureNovelException.Error.HACK_DETECTED.getStatusCode();
                    errResponse.cause = "请求速率限制";
                    res.setStatus(errResponse.status);
                    res.setContentType("application/json; charset=utf-8");
                    ServletOutputStream output = res.getOutputStream();
                    output.write(AppConfig.objectMapper.writeValueAsBytes(errResponse));
                    output.flush();
                    output.close();
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
        public long[] lastRequestsTimeMillis = new long[COLLECT_COUNT];
        public int blockSeconds = -1;
    }

}
