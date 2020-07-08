package net.wlgzs.futurenovel.service;

import net.wlgzs.futurenovel.dao.AccountDao;
import net.wlgzs.futurenovel.model.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountDao accountDao;

    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public Account getAccount(String username) {
        return accountDao.getAccountByUsername(username);
    }

    public Account getAccount(String username, String password) {
        return accountDao.getAccountByUsernameAndPassword(username, password);
    }

    @Transactional
    public void insertAccount(Account account) {
        accountDao.insertAccount(account);
    }

    @Transactional
    public void updateAccount(Account account) {
        accountDao.updateAccount(account);
    }
}
