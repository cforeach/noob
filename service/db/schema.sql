CREATE TABLE t_user ( id BIGINT(19) NOT NULL AUTO_INCREMENT, headimgurl VARCHAR(80) DEFAULT NULL COMMENT '用户头像', update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP , nickname VARCHAR(20) DEFAULT NULL COMMENT '普通用户昵称', unionid VARCHAR(20) DEFAULT NULL COMMENT '微信用户统一标识', PRIMARY KEY (id), UNIQUE KEY udx_user_unionid (unionid) ) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 ;
