package com.helen.eduedu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批改作业请求
 */
@Data
public class HomeworkReviewRequest {

    @NotNull(message = "分数不能为空")
    private BigDecimal score;

    private String comment;

    /** 状态: 1-已批改 2-已退回 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
