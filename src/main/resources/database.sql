CREATE TABLE `novel_db`.`accounts`
(
    `userName`      varchar(64)      NOT NULL,
    `uid`           binary(16)       NOT NULL,
    `userPass`      varchar(255)     NOT NULL,
    `email`         varchar(64)      NOT NULL,
    `phone`         varchar(15)      NULL,
    `registerIP`    varchar(255)     NOT NULL,
    `lastLoginIP`   varchar(255)     NULL,
    `registerDate`  datetime         NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `lastLoginDate` datetime         NULL,
    `status`        tinyint UNSIGNED NOT NULL DEFAULT 0,
    `permission`    tinyint          NOT NULL DEFAULT 0,
    `experience`    bigint UNSIGNED  NOT NULL DEFAULT 1,
    `profileImgUrl` varchar(255)     NULL,
    PRIMARY KEY (`uid`),
    INDEX `name_index` (`userName`) USING BTREE,
    INDEX `email_index` (`email`) USING BTREE
);