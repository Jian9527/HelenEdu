package com.helen.eduedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helen.eduedu.common.RoleEnum;
import com.helen.eduedu.entity.*;
import com.helen.eduedu.mapper.*;
import com.helen.eduedu.vo.ClassRankVO;
import com.helen.eduedu.vo.DashboardOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据看板服务
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EduClassMapper eduClassMapper;
    private final SysUserMapper sysUserMapper;
    private final EduHomeworkMapper eduHomeworkMapper;
    private final EduHomeworkSubmitMapper eduHomeworkSubmitMapper;
    private final EduClassStudentMapper eduClassStudentMapper;

    /**
     * 获取总览数据
     */
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();

        // 总班级数（正常状态）
        Long totalClasses = eduClassMapper.selectCount(
                new LambdaQueryWrapper<EduClass>().eq(EduClass::getStatus, 1)
        );
        vo.setTotalClasses(totalClasses.intValue());

        // 总学生数
        Long totalStudents = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, RoleEnum.STUDENT.getCode()).eq(SysUser::getStatus, 1)
        );
        vo.setTotalStudents(totalStudents.intValue());

        // 总教师数
        Long totalTeachers = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, RoleEnum.TEACHER.getCode()).eq(SysUser::getStatus, 1)
        );
        vo.setTotalTeachers(totalTeachers.intValue());

        // 本周布置作业数
        LocalDateTime weekStart = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        Long weekHomeworkCount = eduHomeworkMapper.selectCount(
                new LambdaQueryWrapper<EduHomework>()
                        .ge(EduHomework::getCreatedAt, weekStart)
                        .eq(EduHomework::getStatus, 1)
        );
        vo.setWeekHomeworkCount(weekHomeworkCount.intValue());

        // 总提交率
        Long totalHomework = eduHomeworkMapper.selectCount(
                new LambdaQueryWrapper<EduHomework>().eq(EduHomework::getStatus, 1)
        );
        Long totalSubmits = eduHomeworkSubmitMapper.selectCount(null);
        if (totalHomework > 0 && totalStudents > 0) {
            BigDecimal rate = BigDecimal.valueOf(totalSubmits)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalHomework * totalStudents), 2, RoundingMode.HALF_UP);
            vo.setSubmitRate(rate.min(BigDecimal.valueOf(100)));
        } else {
            vo.setSubmitRate(BigDecimal.ZERO);
        }

        // 平均分数（已批改的）
        List<EduHomeworkSubmit> reviewedSubmits = eduHomeworkSubmitMapper.selectList(
                new LambdaQueryWrapper<EduHomeworkSubmit>().isNotNull(EduHomeworkSubmit::getScore)
        );
        if (!reviewedSubmits.isEmpty()) {
            BigDecimal totalScore = reviewedSubmits.stream()
                    .map(EduHomeworkSubmit::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setAvgScore(totalScore.divide(BigDecimal.valueOf(reviewedSubmits.size()), 2, RoundingMode.HALF_UP));
        } else {
            vo.setAvgScore(BigDecimal.ZERO);
        }

        return vo;
    }

    /**
     * 获取班级排行
     */
    public List<ClassRankVO> getClassRank() {
        List<EduClass> classes = eduClassMapper.selectList(
                new LambdaQueryWrapper<EduClass>().eq(EduClass::getStatus, 1)
        );

        List<ClassRankVO> rankList = new ArrayList<>();
        for (EduClass eduClass : classes) {
            ClassRankVO rank = new ClassRankVO();
            rank.setClassId(eduClass.getId());
            rank.setClassName(eduClass.getName());

            // 学生数
            Long studentCount = eduClassStudentMapper.selectCount(
                    new LambdaQueryWrapper<EduClassStudent>().eq(EduClassStudent::getClassId, eduClass.getId())
            );
            rank.setStudentCount(studentCount.intValue());

            // 作业数
            Long homeworkCount = eduHomeworkMapper.selectCount(
                    new LambdaQueryWrapper<EduHomework>().eq(EduHomework::getClassId, eduClass.getId())
            );
            rank.setHomeworkCount(homeworkCount.intValue());

            // 提交数
            List<EduHomework> homeworks = eduHomeworkMapper.selectList(
                    new LambdaQueryWrapper<EduHomework>().eq(EduHomework::getClassId, eduClass.getId())
            );
            List<Long> hwIds = homeworks.stream().map(EduHomework::getId).collect(Collectors.toList());

            int submitCount = 0;
            if (!hwIds.isEmpty()) {
                Long count = eduHomeworkSubmitMapper.selectCount(
                        new LambdaQueryWrapper<EduHomeworkSubmit>().in(EduHomeworkSubmit::getHomeworkId, hwIds)
                );
                submitCount = count.intValue();
            }
            rank.setSubmitCount(submitCount);

            // 提交率
            int expectedSubmits = rank.getStudentCount() * rank.getHomeworkCount();
            if (expectedSubmits > 0) {
                rank.setSubmitRate(BigDecimal.valueOf(submitCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(expectedSubmits), 2, RoundingMode.HALF_UP));
            } else {
                rank.setSubmitRate(BigDecimal.ZERO);
            }

            rankList.add(rank);
        }

        // 按提交率降序排序
        rankList.sort((a, b) -> b.getSubmitRate().compareTo(a.getSubmitRate()));
        return rankList;
    }
}
