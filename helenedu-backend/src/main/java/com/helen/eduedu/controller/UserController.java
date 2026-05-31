package com.helen.eduedu.controller;

import com.helen.eduedu.common.PageResult;
import com.helen.eduedu.common.R;
import com.helen.eduedu.dto.UserRequest;
import com.helen.eduedu.security.RequireRole;
import com.helen.eduedu.service.UserService;
import com.helen.eduedu.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequireRole({3})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "创建用户")
    @PostMapping
    public R<Long> createUser(@Valid @RequestBody UserRequest request) {
        return R.ok(userService.createUser(request));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public R<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        userService.updateUser(id, request);
        return R.ok();
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/{id}/toggle-status")
    public R<Void> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public R<PageResult<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) String keyword) {
        return R.ok(userService.getUserList(page, size, role, keyword));
    }

    @Operation(summary = "获取所有教师")
    @GetMapping("/teachers")
    @RequireRole({2, 3})
    public R<List<UserVO>> getAllTeachers() {
        return R.ok(userService.getAllTeachers());
    }

    @Operation(summary = "获取所有学生")
    @GetMapping("/students")
    @RequireRole({2, 3})
    public R<List<UserVO>> getAllStudents() {
        return R.ok(userService.getAllStudents());
    }
}
