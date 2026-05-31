package com.helen.eduedu.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业详情响应
 */
@Data
public class HomeworkVO {
    private Long id;
    private String title;
    private String content;
    private Long classId;
    private String className;
    private Long teacherId;
    private String teacherName;
    private String subject;
    private LocalDateTime deadline;
    private List<String> attachmentUrls;
    private Integer status;
    private Integer targetType;
    private LocalDateTime createdAt;

    /** 指定学生ID列表（教师视角，targetType=1时返回） */
    private List<Long> targetStudentIds;
    /** 指定学生名字列表（教师视角，targetType=1时返回） */
    private List<String> targetStudentNames;

    /** 提交统计（教师视角） */
    private Integer totalCount;
    private Integer submitCount;
    private Integer reviewedCount;

    /** 学生提交状态（学生视角） */
    private Integer mySubmitStatus; // 0-未提交 1-已提交 2-已批改 3-已退回
    private String mySubmitId;
}
