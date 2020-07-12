package net.wlgzs.futurenovel.service;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.dao.AccountDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccountService {
    private final AccountDao accountDao;

    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public Account getAccount(@NonNull UUID uid) {
        try {
            return accountDao.getAccount(uid);
        } catch (DataAccessException e) {
            throw new FutureNovelException(e.getLocalizedMessage(), e);
        }
    }

    public Account getAccount(@NonNull String username) {
        try {
            return accountDao.getAccountByUsername(username);
        } catch (DataAccessException e) {
            throw new FutureNovelException(e.getLocalizedMessage(), e);
        }
    }

    public Account login(@NotNull String username, @NotNull String password) {
        try {
            var account = accountDao.getAccountForLogin(username);
            if (BCrypt.checkpw(password, account.getUserPass()))
                return account;
        } catch (DataAccessException e) {
            log.warn("登录异常", e);
        }
        throw new FutureNovelException(FutureNovelException.Error.INVALID_PASSWORD);
    }

    @Transactional
    public void register(@NonNull Account account) {
        try {
            int ret = accountDao.insertAccount(account);
            if (ret != 1)
                throw new FutureNovelException("逐步添加操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public int updateAccount(Account account) {
        try {
            return accountDao.updateAccount(account);
        } catch (DataAccessException e) {
            throw new FutureNovelException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public int updateAccountExperience(Account account) {
        try {
            return accountDao.updateExperience(account);
        } catch (DataAccessException e) {
            throw new FutureNovelException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public int unRegister(Account account) {
        try {
            return accountDao.deleteAccount(account);
        } catch (DataAccessException e) {
            throw new FutureNovelException(e.getLocalizedMessage(), e);
        }
    }

    public boolean isUsernameUsed(@NonNull String username) {
        return accountDao.getAccountSizeByUsername(username) > 0;
    }

    public boolean isEmailUsed(@NonNull String email) {
        return accountDao.getAccountSizeByEmail(email) > 0;
    }
}
