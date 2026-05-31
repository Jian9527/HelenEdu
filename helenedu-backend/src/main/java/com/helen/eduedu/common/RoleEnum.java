package com.helen.eduedu.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色枚举
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {
    STUDENT(1, "学生"),
    TEACHER(2, "教师"),
    ADMIN(3, "管理员");

    private final int code;
    private final String desc;

    public static RoleEnum fromCode(int code) {
        for (RoleEnum role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的角色代码: " + code);
    }
}
