package com.helen.eduedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户管理请求
 */
@Data
public class UserRequest {

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String phone;

    @NotNull(message = "角色不能为空")
    private Integer role;

    private String avatarUrl;

    /** 教师科目 */
    private String subject;

    /** 性别: 1-男 2-女 */
    private Integer gender;

    /** 家长联系方式（微信/手机号） */
    private String parentContact;

    /** 年级 */
    private String grade;

    /** 分配班级ID（仅学生角色） */
    private Long classId;
}
