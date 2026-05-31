package com.helen.eduedu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 作业指定学生关联实体
 */
@Data
@TableName("edu_homework_student")
public class EduHomeworkStudent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作业ID */
    private Long homeworkId;

    /** 学生ID */
    private Long studentId;
}
