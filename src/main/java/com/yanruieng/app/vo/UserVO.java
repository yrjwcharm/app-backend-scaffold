package com.yanruieng.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserVO {
    private String nickName;
    private String avatarUrl;
    private Integer status;
}
