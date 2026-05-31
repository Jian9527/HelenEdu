package com.helen.eduedu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求
 */
@Data
public class WxLoginRequest {

    @NotBlank(message = "code不能为空")
    private String code;

    /** 用户信息（可选，首次登录时传入） */
    private String nickName;
    private String avatarUrl;
}
