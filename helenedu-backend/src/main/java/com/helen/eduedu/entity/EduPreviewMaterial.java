package com.helen.eduedu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预习资料实体
 */
@Data
@TableName(value = "edu_preview_material", autoResultMap = true)
public class EduPreviewMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 资料标题 */
    private String title;

    /** 资料描述 */
    private String description;

    /** 所属班级 */
    private Long classId;

    /** 发布教师 */
    private Long teacherId;

    /** 科目 */
    private String subject;

    /** 文件URL列表 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;

    /** 状态: 0-下架 1-发布 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
