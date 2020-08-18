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

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.packet.Requests;
import net.wlgzs.futurenovel.dao.AccountDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
            Account account = accountDao.getAccount(uid);
            if (account == null) throw new FutureNovelException(FutureNovelException.Error.ITEM_NOT_FOUND, "找不到该用户");
            return account;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public Account getAccount(@NonNull String username) {
        try {
            Account account = accountDao.getAccountForLogin(username);
            if (account == null) throw new FutureNovelException(FutureNovelException.Error.ILLEGAL_ARGUMENT, "找不到该用户");
            return account;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Nullable
    public List<Account> getAllAccount(int page, int perPage) {
        long offset = (page - 1) * perPage;
        var result = accountDao.getAccountForAdmin(offset, perPage, "`registerDate` DESC");
        if (result == null || result.isEmpty()) return null;
        return result;
    }

    public int getAllAccountPages(int perPage) {
        long total = size();
        return (int) (total / perPage + (total >= perPage ? 0 : 1));
    }

    public long size() {
        return accountDao.size();
    }

    public Account login(@NonNull String username, @NonNull String password) {
        try {
            var account = accountDao.getAccountForLogin(username);
            if (account != null && BCrypt.checkpw(password, account.getUserPass()))
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
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
        } catch (DuplicateKeyException e) {
            throw new FutureNovelException(FutureNovelException.Error.USER_EXIST);
        } catch (DataAccessException e) {
            throw new FutureNovelException(e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public boolean updateAccount(@NonNull Account account) {
        try {
            return 1 == accountDao.updateAccount(account);
        } catch (DuplicateKeyException e) {
            throw new FutureNovelException(FutureNovelException.Error.USER_EXIST);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public boolean editAccount(@NonNull Account account, @NonNull Requests.EditAccountRequest toEdit) {
        try {
            if (toEdit.uid == null) toEdit.uid = UUID.fromString(toEdit.uuid);

            // 如果是管理员编辑其他账号，则检查权限，如果是编辑自己的账号，则某些信息不能自己修改
            if (!account.getUid().equals(toEdit.uid)) {
                account.checkPermission(Account.Permission.ADMIN);
            } else {
                toEdit.vip = null;
                toEdit.status = null;
                toEdit.permission = null;
            }
            if (checkAllNull(toEdit.userName,
                toEdit.password,
                toEdit.email,
                toEdit.phone,
                toEdit.profileImgUrl,
                toEdit.vip,
                toEdit.status,
                toEdit.permission)) return false;
            if (toEdit.password != null) toEdit.password = BCrypt.hashpw(toEdit.password, BCrypt.gensalt());
            return 1 == accountDao.editAccount(toEdit);
        } catch (DuplicateKeyException e) {
            throw new FutureNovelException(FutureNovelException.Error.USER_EXIST);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public int updateAccountExperience(@NonNull Account account) {
        try {
            return accountDao.updateExperience(account);
        } catch (DuplicateKeyException e) {
            throw new FutureNovelException(FutureNovelException.Error.USER_EXIST);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public int unRegister(@NonNull Account account) {
        try {
            return accountDao.deleteAccount(account);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public boolean isUsernameUsed(@NonNull String username, @Nullable UUID except) {
        return accountDao.getAccountSizeByUsername(username, except) > 0;
    }

    public boolean isEmailUsed(@NonNull String email, @Nullable UUID except) {
        return accountDao.getAccountSizeByEmail(email, except) > 0;
    }



    private boolean checkAllNull(Object ... objects) {
        for (var o : objects) {
            if (o instanceof CharSequence) {
                if (((CharSequence) o).length() != 0) return false;
            } else if (o != null) return false;
        }
        return true;
    }

}
