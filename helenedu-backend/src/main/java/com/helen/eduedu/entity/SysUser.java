package com.helen.eduedu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信openid */
    private String openid;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 角色: 1-学生 2-教师 3-管理员 */
    private Integer role;

    /** 头像 */
    private String avatarUrl;

    /** 教师科目 */
    private String subject;

    /** 性别: 1-男 2-女 */
    private Integer gender;

    /** 家长联系方式（微信/手机号） */
    private String parentContact;

    /** 年级 */
    private String grade;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
