package net.wlgzs.futurenovel.service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import net.wlgzs.futurenovel.bean.Token;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class TokenStore {
    private final ConcurrentLinkedHashMap<String, Token> tokenMap = new ConcurrentLinkedHashMap.Builder<String, Token>()
        .maximumWeightedCapacity(1024 * 1024)
        .listener((key, value) -> value.setLastUse(System.currentTimeMillis()))
        .build();

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
        var token = new Token(clientAgent, clientIp, account.getUid());
        tokenMap.put(token.getToken(), token);
        return token;
    }

    public Token verifyToken(@NonNull String tokenStr, @NonNull UUID uid, @NonNull String clientIp, @NonNull String clientAgent) {
        var token = tokenMap.getQuietly(tokenStr);
        if (token == null ||
            System.currentTimeMillis() - token.getLastUse() > Duration.ofDays(7).toMillis() ||
            !token.equals(new Token(tokenStr, clientIp, clientAgent, uid))) {
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
}
