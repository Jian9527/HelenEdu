package com.helen.eduedu.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预习资料响应
 */
@Data
public class PreviewMaterialVO {
    private Long id;
    private String title;
    private String description;
    private Long classId;
    private String className;
    private Long teacherId;
    private String teacherName;
    private String subject;
    private List<String> fileUrls;
    private Integer status;
    private LocalDateTime createdAt;
}
