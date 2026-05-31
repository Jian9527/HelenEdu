package com.helen.eduedu.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据看板总览
 */
@Data
public class DashboardOverviewVO {
    /** 总班级数 */
    private Integer totalClasses;
    /** 总学生数 */
    private Integer totalStudents;
    /** 总教师数 */
    private Integer totalTeachers;
    /** 本周布置作业数 */
    private Integer weekHomeworkCount;
    /** 总提交率 */
    private BigDecimal submitRate;
    /** 平均分数 */
    private BigDecimal avgScore;
}
