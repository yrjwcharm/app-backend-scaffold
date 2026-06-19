package com.example.app.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MybatisPlusConfig implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        //createAt、updateAt 、createBy、updateBy
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createUser", Long.class, 0L);
        this.strictInsertFill(metaObject, "updateUser", Long.class, 0L);
    }
    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateUser", Long.class, 0L);
    }
}
