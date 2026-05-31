package com.helen.eduedu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业提交实体
 */
@Data
@TableName(value = "edu_homework_submit", autoResultMap = true)
public class EduHomeworkSubmit {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作业ID */
    private Long homeworkId;

    /** 学生ID */
    private Long studentId;

    /** 提交内容 */
    private String content;

    /** 附件URL列表 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> attachmentUrls;

    /** 分数 */
    private BigDecimal score;

    /** 教师评语 */
    private String comment;

    /** 状态: 0-已提交 1-已批改 2-已退回 */
    private Integer status;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 批改时间 */
    private LocalDateTime reviewTime;
}
