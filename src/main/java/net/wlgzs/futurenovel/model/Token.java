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

package net.wlgzs.futurenovel.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.AutomapConstructor;

@Getter
@Slf4j
public class Token implements Serializable {

    private static final long serialVersionUID = 3869142965255874030L;

    @Setter
    @NonNull
    private long lastUse;

    final private UUID accountUid;

    final private String token;

    public static class Builder {
        private String clientIp;
        private String clientAgent;
        private UUID accountUid;

        public Builder setClientIp(@NonNull String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder setClientAgent(@NonNull String clientAgent) {
            this.clientAgent = clientAgent;
            return this;
        }

        public Builder setAccountUid(@NonNull UUID accountUid) {
            this.accountUid = accountUid;
            return this;
        }

        public Token build() {
            return new Token(clientIp, clientAgent, accountUid);
        }

    }

    // 用于 MyBatis 结果映射
    @AutomapConstructor
    private Token(@NonNull String token, @NonNull UUID accountUid, @NonNull long lastUse) {
        this.lastUse = lastUse;
        this.accountUid = accountUid;
        this.token = token;
    }

    private Token(@NonNull String clientIp, @NonNull String clientAgent, @NonNull UUID accountUid) {
        this.accountUid = accountUid;
        byte[] total = new byte[32];
        String seed = clientIp + "!!!" + clientAgent + "@@@" + accountUid.toString() + "???";
        byte[] most;
        try {
            var messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(seed.getBytes());
            most = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            most = seed.getBytes();
            log.error("MessageDigest algorithm is not found: {}", e.getLocalizedMessage());
        }
        var random = new SecureRandom();
        byte[] least = new byte[16];
        random.nextBytes(least);
        System.arraycopy(most, 0, total, 0, 16);
        System.arraycopy(least, 0, total, 16, 16);
        this.token = toHex(total);
        lastUse = System.currentTimeMillis();
    }

    private String toHex(byte[] bytes) {
        var sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(String.format("%02x", aByte & 0xff));
        }
        return sb.toString();
    }

    public boolean checkToken(@NonNull String clientIp, @NonNull String clientAgent, @NonNull UUID accountUid) {
        if (!this.accountUid.equals(accountUid)) return false;
        String seed = clientIp + "!!!" + clientAgent + "@@@" + accountUid.toString() + "???";
        byte[] most;
        try {
            var messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(seed.getBytes());
            most = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            most = seed.substring(0, 16).getBytes();
            log.error("MessageDigest algorithm is not found: {}", e.getLocalizedMessage());
        }
        return toHex(most).equals(token.substring(0, 32));
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
        return accountUid.equals(token.accountUid) &&
            this.token.equals(token.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountUid, token);
    }

}
