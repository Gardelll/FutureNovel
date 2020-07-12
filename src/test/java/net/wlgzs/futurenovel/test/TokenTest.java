package net.wlgzs.futurenovel.test;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.model.Token;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class TokenTest {

    @Test
    public void verifyTest() {
        var token = new Token.Builder().setClientIp("145.78.143.9")
                .setClientAgent("Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0")
                .setAccountUid(UUID.fromString("d46d71c7-b0cf-45c6-a082-0ff75281d959")).build();
        var tokenStr = token.getToken();
        log.info("token={}", tokenStr);
        Assert.assertTrue(token.checkToken("145.78.143.9",
                "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0",
                UUID.fromString("d46d71c7-b0cf-45c6-a082-0ff75281d959")));
        Assert.assertFalse(token.checkToken("145.78.143.9",
                "Agent changed",
                UUID.fromString("d46d71c7-b0cf-45c6-a082-0ff75281d959")));
        Assert.assertFalse(token.checkToken("IP changed",
                "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0",
                UUID.fromString("d46d71c7-b0cf-45c6-a082-0ff75281d959")));
        Assert.assertFalse(token.checkToken("145.78.143.9",
                "Mozilla/5.0 (X11; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0",
                UUID.randomUUID()));
    }

}
