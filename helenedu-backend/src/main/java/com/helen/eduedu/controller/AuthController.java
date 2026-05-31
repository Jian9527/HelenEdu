package com.helen.eduedu.controller;

import com.helen.eduedu.common.R;
import com.helen.eduedu.dto.WxLoginRequest;
import com.helen.eduedu.service.AuthService;
import com.helen.eduedu.vo.LoginVO;
import com.helen.eduedu.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证模块")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "微信登录")
    @PostMapping("/wx-login")
    public R<LoginVO> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return R.ok(authService.wxLogin(request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public R<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(authService.getUserInfo(userId));
    }
}
