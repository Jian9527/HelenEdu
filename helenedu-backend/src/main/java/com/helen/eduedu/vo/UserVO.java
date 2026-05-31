package com.helen.eduedu.vo;

import lombok.Data;

/**
 * 用户信息响应
 */
@Data
public class UserVO {
    private Long id;
    private String name;
    private String phone;
    private Integer role;
    private String roleName;
    private String avatarUrl;
    private Integer status;
    private String subject;
    private Integer gender;
    private String parentContact;
    private String grade;
    private Long classId;
    private String className;
}
