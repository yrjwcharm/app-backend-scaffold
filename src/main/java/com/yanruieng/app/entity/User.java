package com.yanruieng.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("nick_name")
    private String nickName;

    @TableField("avatar_url")
    private String avatarUrl;
    @TableField("status")
    private Integer status;

}

