package net.wlgzs.futurenovel.bean;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class Token {
    @Setter
    private long lastUse;

    @NonNull
    final private String clientIp;

    @NonNull
    final private String clientAgent;

    @NonNull
    final private UUID accountUid;

    @NonNull
    final private String token;

    public Token(@NonNull String token, @NonNull String clientIp, @NonNull String clientAgent, @NonNull UUID accountUid) {
        this.clientIp = clientIp;
        this.clientAgent = clientAgent;
        this.accountUid = accountUid;
        this.token = token;
    }

    public Token(@NonNull String clientIp, @NonNull String clientAgent, @NonNull UUID accountUid) {
        this.clientIp = clientIp;
        this.clientAgent = clientAgent;
        this.accountUid = accountUid;
        var random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        token = toHex(bytes);
        lastUse = System.currentTimeMillis();
    }

    private String toHex(byte[] bytes) {
        var sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toHexString(aByte & 0xff));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return clientIp.equals(token.clientIp) &&
            clientAgent.equals(token.clientAgent) &&
            accountUid.equals(token.accountUid) &&
            this.token.equals(token.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIp, clientAgent, accountUid, token);
    }

}
