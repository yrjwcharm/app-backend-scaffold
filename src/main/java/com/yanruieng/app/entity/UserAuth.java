package com.yanruieng.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_auth")
public class UserAuth extends BaseEntity {
    @TableId
    private Long id;
    private Long userId;
    private String identityType; // password / phone / wechat
    private String identifier;   // username / phone / openid
    private String credential;   // password hash / openid credential
    private Integer status;
}
