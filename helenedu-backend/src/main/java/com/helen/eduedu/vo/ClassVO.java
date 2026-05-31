package com.helen.eduedu.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级信息响应
 */
@Data
public class ClassVO {
    private Long id;
    private String name;
    private String grade;
    private Long teacherId;
    private String teacherName;
    private Integer status;
    private Integer studentCount;
    private Integer teacherCount;
    private LocalDateTime createdAt;
}
