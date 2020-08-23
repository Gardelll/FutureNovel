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

package net.wlgzs.futurenovel.service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.AppProperties;
import net.wlgzs.futurenovel.dao.TokenDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.Token;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;
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

    private final AppProperties futureNovelConfig;

    public TokenStore(TokenDao tokenDao,
                      ScheduledExecutorService executor,
                      AppProperties futureNovelConfig) {
        this.tokenDao = tokenDao;
        this.executor = executor;
        this.futureNovelConfig = futureNovelConfig;
        var tokens = this.tokenDao.getAll();
        tokens.forEach(token -> tokenMap.put(token.getToken(), token));
        future = executor.scheduleAtFixedRate(this::saveTokens, 1, futureNovelConfig.getToken().getSavePeriod(), TimeUnit.MINUTES);
    }

    public void removeToken(Token token) {
        removeToken(token.getToken());
    }

    public void removeToken(String accessToken) {
        synchronized (tokenMap) {
            tokenMap.remove(accessToken);
        }
    }

    public void removeAll(UUID accountId) {
        tokenMap.forEach((k, v) -> {
            if (v.getAccountUid().equals(accountId)) synchronized (tokenMap) {
                tokenMap.remove(k, v);
            }
        });
    }

    public Token acquireToken(@NonNull Account account, @NonNull String clientIp, @NonNull String clientAgent) {
        if (clientAgent.isEmpty()) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, "浏览器 UA 不能为空");
        var token = new Token.Builder()
                .setClientIp(clientIp)
                .setClientAgent(clientAgent)
                .setAccountUid(account.getUid())
                .build();
        synchronized (tokenMap) { tokenMap.put(token.getToken(), token); }
        return token;
    }

    public Token verifyToken(@NonNull String tokenStr, @NonNull UUID uid, @NonNull String clientIp, @NonNull String clientAgent) {
        var token = tokenMap.getQuietly(tokenStr);
        if (token == null ||
                !token.checkToken(clientIp, clientAgent, uid) ||
                System.currentTimeMillis() - token.getLastUse() > Duration.ofDays(futureNovelConfig.getToken().getExpire()).toMillis()) {
            if (token != null) removeToken(token);
            return null;
        }
        token.setLastUse(System.currentTimeMillis());
        return token;
    }

    @Transactional
    public void saveTokens() {
        synchronized (tokenMap) {
            if (executor.isShutdown()) return;
            var tokens = this.tokenDao.getAll();
            tokens.forEach(token -> {
                var old = tokenMap.put(token.getToken(), token);
                if (old != null) {
                    token.setLastUse(Math.max(old.getLastUse(), token.getLastUse()));
                }
            });
            tokenDao.clear();
            if (tokenMap.isEmpty()) return;
            int result = tokenDao.insertAll(tokenMap.values()
                    .stream()
                    .filter(token -> System.currentTimeMillis() - token.getLastUse() < Duration.ofDays(futureNovelConfig.getToken().getExpire()).toMillis())
                    .collect(Collectors.toList()));
            log.debug("Saved {} token(s)", result);
        }
    }

    @Override
    public void destroy() {
        future.cancel(true);
        saveTokens();
        log.info("Service destroying");
    }
}
