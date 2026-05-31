package com.helen.eduedu.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 班级排行数据
 */
@Data
public class ClassRankVO {
    private Long classId;
    private String className;
    private Integer studentCount;
    private Integer homeworkCount;
    private Integer submitCount;
    private BigDecimal submitRate;
    private BigDecimal avgScore;
}
