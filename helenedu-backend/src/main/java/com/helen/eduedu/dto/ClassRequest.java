package com.helen.eduedu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新班级请求
 */
@Data
public class ClassRequest {

    @NotBlank(message = "班级名称不能为空")
    private String name;

    private String grade;

    private Long teacherId;
}
