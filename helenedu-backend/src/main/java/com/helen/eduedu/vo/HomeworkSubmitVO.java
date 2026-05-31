package com.helen.eduedu.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业提交详情响应
 */
@Data
public class HomeworkSubmitVO {
    private Long id;
    private Long homeworkId;
    private String homeworkTitle;
    private Long studentId;
    private String studentName;
    private String content;
    private List<String> attachmentUrls;
    private BigDecimal score;
    private String comment;
    private Integer status;
    private String statusName;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
}
