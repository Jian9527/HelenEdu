package com.helen.eduedu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加班级成员请求
 */
@Data
public class ClassMemberRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 科目（教师专用） */
    private String subject;
}
