START TRANSACTION;
SET time_zone = "+08:00";
CREATE TABLE `accounts`
(
    `userName`      varchar(64)         NOT NULL,
    `uid`           binary(16)          NOT NULL,
    `userPass`      varchar(255)        NOT NULL,
    `email`         varchar(64)         NOT NULL,
    `phone`         varchar(15)                  DEFAULT NULL,
    `registerIP`    varchar(255)        NOT NULL,
    `lastLoginIP`   varchar(255)                 DEFAULT NULL,
    `registerDate`  datetime            NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP,
    `lastLoginDate` datetime                     DEFAULT NULL,
    `status`        tinyint(3) UNSIGNED NOT NULL DEFAULT '0',
    `permission`    tinyint(4)          NOT NULL DEFAULT '0',
    `experience`    bigint(20) UNSIGNED NOT NULL DEFAULT '1',
    `profileImgUrl` varchar(255)                 DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `accounts`
    ADD PRIMARY KEY (`uid`),
    ADD UNIQUE KEY `name_index` (`userName`) USING BTREE,
    ADD UNIQUE KEY `email_index` (`email`) USING BTREE,
    ADD UNIQUE KEY `phone_index` (`phone`) USING BTREE;
COMMIT;