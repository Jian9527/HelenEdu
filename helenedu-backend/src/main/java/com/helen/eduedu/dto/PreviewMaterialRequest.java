package com.helen.eduedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 发布/更新预习资料请求
 */
@Data
public class PreviewMaterialRequest {

    @NotBlank(message = "资料标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    private String subject;

    private List<String> fileUrls;

    /** 状态: 0-下架 1-发布 */
    private Integer status;
}
