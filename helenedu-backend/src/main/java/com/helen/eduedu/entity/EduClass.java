package com.helen.eduedu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级实体
 */
@Data
@TableName("edu_class")
public class EduClass {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级名称 */
    private String name;

    /** 年级 */
    private String grade;

    /** 班主任ID */
    private Long teacherId;

    /** 状态: 0-解散 1-正常 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
