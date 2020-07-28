package net.wlgzs.futurenovel.test;

import java.math.BigInteger;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.dao.AccountDao;
import net.wlgzs.futurenovel.model.Account;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DaoTestContext.class)
@Transactional
@Slf4j
public class DatabaseTest {
    @Autowired
    private AccountDao accountDao;

    @Test
    void insertTest() {
        Account account = new Account();
        log.info("account uuid = {}", account.getUid());
        account.setUserName(UUID.randomUUID().toString());
        account.setUserPass("not my password");
        account.setEmail(UUID.randomUUID() + "@bar.cn");
        account.setPhone("10086");
        account.setStatus(Account.Status.FINE);
        account.setPermission(Account.Permission.USER);
        account.setRegisterIP("[::1]");
        account.setRegisterDate(new Date());
        account.setExperience(BigInteger.valueOf(500));
        account.setProfileImgUrl("https://www.example.com/image.png");
        accountDao.insertAccount(account);
        Account got = accountDao.getAccount(account.getUid());
        Assert.assertNotNull(got);
        Assert.assertEquals(account.getUid(), got.getUid());
        Assert.assertEquals(account.getUidNum(), got.getUidNum());
        log.info("account: {}", got);
    }
}
