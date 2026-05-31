package com.helen.eduedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建/更新作业请求
 */
@Data
public class HomeworkRequest {

    @NotBlank(message = "作业标题不能为空")
    private String title;

    private String content;

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    private String subject;

    private LocalDateTime deadline;

    private List<String> attachmentUrls;

    /** 状态: 0-草稿 1-已发布 */
    private Integer status;

    /** 目标类型: 0-全班 1-指定学生 */
    private Integer targetType;

    /** 指定学生ID列表（targetType=1时使用） */
    private List<Long> studentIds;
}
