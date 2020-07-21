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
    `registerDate`  datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `lastLoginDate` datetime                     DEFAULT NULL,
    `status`        tinyint(3) UNSIGNED NOT NULL DEFAULT 0,
    `isVIP`         tinyint(1)          NOT NULL DEFAULT 0,
    `permission`    tinyint(4)          NOT NULL DEFAULT 0,
    `experience`    bigint(20) UNSIGNED NOT NULL DEFAULT 1,
    `profileImgUrl` varchar(255)                 DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `chapter`
(
    `uniqueId`  binary(16)   NOT NULL,
    `fromNovel` binary(16)   NOT NULL,
    `title`     varchar(255) NOT NULL,
    `sections`  mediumtext   NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `novel_index`
(
    `uniqueId`    binary(16)   NOT NULL,
    `uploader`    binary(16)   NOT NULL,
    `title`       varchar(255) NOT NULL,
    `copyright`   tinyint(3)   NOT NULL DEFAULT 1,
    `authors`     varchar(255) NOT NULL,
    `description` text         NOT NULL,
    `rating`      tinyint(3)   NOT NULL,
    `tags`        varchar(255) NOT NULL,
    `series`      varchar(255) NOT NULL,
    `publisher`   varchar(255) NOT NULL,
    `pubdate`     datetime     NOT NULL,
    `coverImgUrl` varchar(128)          DEFAULT NULL,
    `chapters`    mediumtext   NOT NULL
) ENGINE = Mroonga
  DEFAULT CHARSET = utf8mb4 COMMENT ='engine "InnoDB"';

-- 如果使用 Mysql: ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

CREATE TABLE `section`
(
    `uniqueId`    binary(16)   NOT NULL,
    `fromChapter` binary(16)   NOT NULL,
    `title`       varchar(255) NOT NULL,
    `text`        mediumtext   NOT NULL
) ENGINE = Mroonga
  DEFAULT CHARSET = utf8mb4 COMMENT ='engine "InnoDB"';

-- 如果使用 Mysql: ENGINE=InnoDB DEFAULT CHARSET=utf8mb4

CREATE TABLE `token_store`
(
    `token`      varchar(64) NOT NULL,
    `accountUid` binary(16)  NOT NULL,
    `lastUse`    bigint(13)  NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `accounts`
    ADD PRIMARY KEY (`uid`),
    ADD UNIQUE KEY `name_index` (`userName`) USING BTREE,
    ADD UNIQUE KEY `email_index` (`email`) USING BTREE,
    ADD UNIQUE KEY `phone_index` (`phone`) USING BTREE;

ALTER TABLE `chapter`
    ADD PRIMARY KEY (`uniqueId`),
    ADD KEY `fromNovel_index` (`fromNovel`),
    ADD KEY `titleIndex` (`title`);

ALTER TABLE `novel_index`
    ADD PRIMARY KEY (`uniqueId`),
    ADD KEY `uploader_index` (`uploader`),
    ADD KEY `rating_index` (`rating`),
    ADD KEY `pubdate_index` (`pubdate`);

ALTER TABLE `novel_index`
    ADD FULLTEXT KEY `info_fulltext_index` (`title`, `authors`, `description`, `tags`, `series`, `publisher`) COMMENT ='tokenizer "TokenMecab"';

-- 如果使用 Mysql: ALTER TABLE `novel_index` ADD FULLTEXT KEY `info_fulltext_index` (`title`,`authors`,`description`,`tags`,`series`,`publisher`) WITH PARSER ngram;

ALTER TABLE `section`
    ADD PRIMARY KEY (`uniqueId`),
    ADD KEY `fromChapter_index` (`fromChapter`);

ALTER TABLE `section`
    ADD FULLTEXT KEY `fulltext_index` (`title`, `text`) COMMENT ='tokenizer "TokenMecab"';

-- 如果使用 Mysql: ALTER TABLE `section` ADD FULLTEXT KEY `fulltext_index` (`title`,`text`) WITH PARSER ngram;

ALTER TABLE `token_store`
    ADD PRIMARY KEY (`token`);

COMMIT;