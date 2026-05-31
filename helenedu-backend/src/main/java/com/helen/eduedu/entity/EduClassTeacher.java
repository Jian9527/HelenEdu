package com.helen.eduedu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 班级教师关联实体
 */
@Data
@TableName("edu_class_teacher")
public class EduClassTeacher {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级ID */
    private Long classId;

    /** 教师ID */
    private Long teacherId;

    /** 科目 */
    private String subject;
}
