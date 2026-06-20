CREATE DATABASE IF NOT EXISTS app_backend DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE app_backend;

DROP TABLE IF EXISTS user;

DROP TABLE IF EXISTS user_auth;
DROP TABLE  IF EXISTS file_info;

CREATE TABLE `user` (
                        `id` BIGINT PRIMARY KEY COMMENT '用户ID，雪花ID',
                        `nick_name` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                        `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
                        `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                        `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用 2注销 3冻结 4待审核',
                        `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
                        `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
                        `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON
UPDATE
	CURRENT_TIMESTAMP COMMENT '更新时间',
                        `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

CREATE TABLE user_auth
(
    id BIGINT NOT NULL COMMENT '雪花算法ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    identity_type VARCHAR(32) NOT NULL COMMENT 'password/phone/wechat/apple',
    identifier VARCHAR(128) NOT NULL COMMENT '账号/手机号/openid',
    credential VARCHAR(255) DEFAULT NULL COMMENT '密码hash或第三方凭证',
--     status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    create_user BIGINT DEFAULT NULL COMMENT '上传人ID',
    update_user BIGINT DEFAULT NULL COMMENT '更新人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE
	CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_identity_identifier (identity_type,
        identifier),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户认证方式表';

CREATE TABLE file_info
(
    id            BIGINT PRIMARY KEY COMMENT '文件ID，雪花算法',

    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_name     VARCHAR(255) NOT NULL COMMENT '存储文件名',
    object_key    VARCHAR(512) NOT NULL COMMENT 'OSS对象Key',
    url           VARCHAR(1024)         DEFAULT NULL COMMENT '文件访问地址，冗余字段',

    bucket_name   VARCHAR(128) NOT NULL COMMENT 'Bucket名称',
    storage_type  TINYINT      NOT NULL DEFAULT 1 COMMENT '存储类型：1-阿里云OSS，2-本地，3-MinIO',

    file_size     BIGINT       NOT NULL COMMENT '文件大小，单位字节',
    content_type  VARCHAR(100)          DEFAULT NULL COMMENT '文件MIME类型',
    file_ext      VARCHAR(20)           DEFAULT NULL COMMENT '文件后缀',
    file_md5      VARCHAR(32)           DEFAULT NULL COMMENT '文件MD5',
--     biz_type      VARCHAR(50) DEFAULT NULL COMMENT '业务类型：avatar/article/course等',
--     biz_id        BIGINT DEFAULT NULL COMMENT '业务ID',
    status        TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，2-上传中，3-上传失败，4-禁用',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否，1-是',

    create_user   BIGINT                DEFAULT NULL COMMENT '上传人ID',
    update_user   BIGINT                DEFAULT NULL COMMENT '更新人ID',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_object_key (object_key),
    KEY           idx_create_user (create_user),
    KEY           idx_file_md5 (file_md5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';



-- CREATE TABLE prompts
-- (
--     id          BIGINT PRIMARY KEY,
--     user_id     BIGINT       NOT NULL,
--     category_id BIGINT,
--
--     title       VARCHAR(200) NOT NULL,
--     description VARCHAR(500),
--
--     content     LONGTEXT     NOT NULL,
--
--     create_time DATETIME,
--     update_time DATETIME
-- );

-- CREATE TABLE skills
-- (
--     id          BIGINT PRIMARY KEY,
--     user_id     BIGINT       NOT NULL,
--
--     title       VARCHAR(200) NOT NULL,
--
--     description VARCHAR(500),
--
--     content     LONGTEXT     NOT NULL,
--
--     version     VARCHAR(20),
--
--     create_time DATETIME,
--     update_time DATETIME
-- );
