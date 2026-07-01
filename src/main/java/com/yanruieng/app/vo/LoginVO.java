package com.yanruieng.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private Integer accessTokenExpireDays;
    private Integer refreshTokenExpireDays;
}
