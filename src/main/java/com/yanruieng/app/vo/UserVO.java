package com.yanruieng.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
}
