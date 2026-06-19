package com.yanruieng.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_code")
public class SmsCode extends BaseEntity {
    @TableId
    private Long id;
    private String phone;
    private String code;
    private String scene;
    private LocalDateTime expireTime;
    private Integer used;
}
