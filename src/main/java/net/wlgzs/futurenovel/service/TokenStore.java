package net.wlgzs.futurenovel.service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.dao.TokenDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.Token;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TokenStore implements DisposableBean {

    private final ConcurrentLinkedHashMap<String, Token> tokenMap = new ConcurrentLinkedHashMap.Builder<String, Token>()
        .maximumWeightedCapacity(1024 * 1024)
        .listener((key, value) -> value.setLastUse(System.currentTimeMillis()))
        .build();

    private final TokenDao tokenDao;

    private final ScheduledExecutorService executor;

    private final ScheduledFuture<?> future;

    private final Properties futureNovelConfig;

    public TokenStore(TokenDao tokenDao, Properties futureNovelConfig) {
        this.tokenDao = tokenDao;
        this.futureNovelConfig = futureNovelConfig;
        var tokens = this.tokenDao.getAll();
        tokens.forEach(token -> tokenMap.put(token.getToken(), token));
        executor = Executors.newScheduledThreadPool(1);
        // Save to database every 10min;
        future = executor.scheduleAtFixedRate(this::saveTokens, 1, Long.parseLong(futureNovelConfig.getProperty("future.token.savePeriod", "10")), TimeUnit.MINUTES);
    }

    public void removeToken(Token token) {
        removeToken(token.getToken());
    }

    public void removeToken(String accessToken) {
        tokenMap.remove(accessToken);
    }

    public void removeAll(Account account) {
        tokenMap.forEach((k, v) -> {
            if (v.getAccountUid().equals(account.getUid()))
                tokenMap.remove(k, v);
        });
    }

    public Token acquireToken(@NonNull Account account, @NonNull String clientIp, @NonNull String clientAgent) {
        if (clientAgent.isEmpty()) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, "浏览器 UA 不能为空");
        var token = new Token.Builder()
                .setClientIp(clientIp)
                .setClientAgent(clientAgent)
                .setAccountUid(account.getUid())
                .build();
        tokenMap.put(token.getToken(), token);
        return token;
    }

    public Token verifyToken(@NonNull String tokenStr, @NonNull UUID uid, @NonNull String clientIp, @NonNull String clientAgent) {
        var token = tokenMap.getQuietly(tokenStr);
        if (token == null ||
                !token.checkToken(clientIp, clientAgent, uid) ||
                System.currentTimeMillis() - token.getLastUse() > Duration.ofDays(Long.parseLong(futureNovelConfig.getProperty("future.token.expire", "7"))).toMillis()) {
            if (token != null) removeToken(token);
            return null;
        }
        token.setLastUse(System.currentTimeMillis());
        return token;
    }

    public Token getToken(@Nullable String token) {
        return Optional.ofNullable(token)
            .map(tokenMap::get)
            .orElseThrow(() -> new FutureNovelException(FutureNovelException.Error.INVALID_TOKEN));
    }

    @Transactional
    public void saveTokens() {
        synchronized (tokenMap) {
            if (executor.isShutdown()) return;
            tokenDao.clear();
            if (tokenMap.isEmpty()) return;
            int result = tokenDao.insertAll(tokenMap.values()
                    .stream()
                    .filter(token -> System.currentTimeMillis() - token.getLastUse() < Duration.ofDays(Long.parseLong(futureNovelConfig.getProperty("future.token.expire", "7"))).toMillis())
                    .collect(Collectors.toList()));
            log.debug("Saved {} token(s)", result);
        }
    }

    @Override
    public void destroy() {
        future.cancel(true);
        saveTokens();
        log.info("Service destroying");
        executor.shutdownNow();
    }
}
