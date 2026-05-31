package com.helen.eduedu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业实体
 */
@Data
@TableName(value = "edu_homework", autoResultMap = true)
public class EduHomework {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作业标题 */
    private String title;

    /** 作业内容/要求 */
    private String content;

    /** 所属班级 */
    private Long classId;

    /** 布置教师 */
    private Long teacherId;

    /** 科目 */
    private String subject;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 附件URL列表 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> attachmentUrls;

    /** 状态: 0-草稿 1-已发布 2-已截止 */
    private Integer status;

    /** 目标类型: 0-全班 1-指定学生 */
    private Integer targetType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
