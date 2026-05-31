package com.helen.eduedu.vo;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String name;
    private Integer role;
    private String avatarUrl;
    private boolean isNewUser;
    private String subject;
}
