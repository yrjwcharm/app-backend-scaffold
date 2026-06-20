package com.yanruieng.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_auth")
public class UserAuth extends BaseEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("identity_type")
    private String identityType; // password / phone / wechat
    @TableField("identifier")
    private String identifier;   // username / phone / openid
    @TableField("credential")
    private String credential;   // password hash / openid credential
    private Long userId;
//    private Integer status; '1正常 0禁用',
}
