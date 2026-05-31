package com.helen.eduedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helen.eduedu.common.BusinessException;
import com.helen.eduedu.common.PageResult;
import com.helen.eduedu.dto.HomeworkRequest;
import com.helen.eduedu.dto.HomeworkReviewRequest;
import com.helen.eduedu.dto.HomeworkSubmitRequest;
import com.helen.eduedu.entity.*;
import com.helen.eduedu.mapper.*;
import com.helen.eduedu.vo.HomeworkSubmitVO;
import com.helen.eduedu.vo.HomeworkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 作业管理服务
 */
@Service
@RequiredArgsConstructor
public class HomeworkService {

    private final EduHomeworkMapper eduHomeworkMapper;
    private final EduHomeworkSubmitMapper eduHomeworkSubmitMapper;
    private final EduClassMapper eduClassMapper;
    private final EduClassStudentMapper eduClassStudentMapper;
    private final EduHomeworkStudentMapper eduHomeworkStudentMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 创建作业（教师）
     */
    @Transactional
    public Long createHomework(Long teacherId, HomeworkRequest request) {
        EduHomework homework = new EduHomework();
        BeanUtils.copyProperties(request, homework);
        System.out.println("copyProperties后: attachmentUrls = " + homework.getAttachmentUrls());
        homework.setTeacherId(teacherId);
        if (homework.getStatus() == null) {
            homework.setStatus(1); // 默认发布
        }
        if (homework.getTargetType() == null) {
            homework.setTargetType(0); // 默认全班
        }
        eduHomeworkMapper.insert(homework);
        System.out.println("插入后作业ID: " + homework.getId());

        // 如果是指定学生，保存学生ID列表
        if (request.getTargetType() != null && request.getTargetType() == 1
                && request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            for (Long studentId : request.getStudentIds()) {
                EduHomeworkStudent hs = new EduHomeworkStudent();
                hs.setHomeworkId(homework.getId());
                hs.setStudentId(studentId);
                eduHomeworkStudentMapper.insert(hs);
            }
        }
        return homework.getId();
    }

    /**
     * 更新作业
     */
    @Transactional
    public void updateHomework(Long id, HomeworkRequest request) {
        EduHomework homework = eduHomeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        System.out.println("更新前原attachmentUrls: " + homework.getAttachmentUrls());
        BeanUtils.copyProperties(request, homework);
        System.out.println("copyProperties后: attachmentUrls = " + homework.getAttachmentUrls());
        eduHomeworkMapper.updateById(homework);
    }

    /**
     * 删除作业
     */
    @Transactional
    public void deleteHomework(Long id) {
        eduHomeworkMapper.deleteById(id);
    }

    /**
     * 获取作业详情
     */
    public HomeworkVO getHomeworkDetail(Long id, Long currentUserId, Integer role) {
        EduHomework homework = eduHomeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        return convertToHomeworkVO(homework, currentUserId, role);
    }

    /**
     * 获取作业列表（教师视角 - 按班级）
     */
    public PageResult<HomeworkVO> getTeacherHomeworkList(Long teacherId, Long classId, int page, int size) {
        Page<EduHomework> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<EduHomework> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduHomework::getTeacherId, teacherId);
        if (classId != null) {
            wrapper.eq(EduHomework::getClassId, classId);
        }
        wrapper.orderByDesc(EduHomework::getCreatedAt);

        Page<EduHomework> result = eduHomeworkMapper.selectPage(pageParam, wrapper);
        List<HomeworkVO> voList = result.getRecords().stream()
                .map(h -> convertToHomeworkVO(h, teacherId, 2))
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), page, size, voList);
    }

    /**
     * 获取作业列表（学生视角）
     */
    public PageResult<HomeworkVO> getStudentHomeworkList(Long studentId, Integer statusFilter, int page, int size) {
        // 获取学生所在班级
        List<EduClassStudent> relations = eduClassStudentMapper.selectList(
                new LambdaQueryWrapper<EduClassStudent>().eq(EduClassStudent::getStudentId, studentId)
        );
        List<Long> classIds = relations.stream().map(EduClassStudent::getClassId).collect(Collectors.toList());

        // 获取指定给该学生的作业ID
        List<EduHomeworkStudent> targetedRelations = eduHomeworkStudentMapper.selectList(
                new LambdaQueryWrapper<EduHomeworkStudent>().eq(EduHomeworkStudent::getStudentId, studentId)
        );
        List<Long> targetedHomeworkIds = targetedRelations.stream()
                .map(EduHomeworkStudent::getHomeworkId).collect(Collectors.toList());

        if (classIds.isEmpty() && targetedHomeworkIds.isEmpty()) {
            return new PageResult<>(0, page, size, List.of());
        }

        Page<EduHomework> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<EduHomework> wrapper = new LambdaQueryWrapper<>();
        // 班级作业（targetType=0）或指定给该学生的作业（targetType=1）
        wrapper.and(w -> {
            if (!classIds.isEmpty()) {
                w.and(inner -> inner.in(EduHomework::getClassId, classIds)
                        .and(inner2 -> inner2.eq(EduHomework::getTargetType, 0)
                                .or().isNull(EduHomework::getTargetType)));
            }
            if (!targetedHomeworkIds.isEmpty()) {
                if (!classIds.isEmpty()) {
                    w.or().in(EduHomework::getId, targetedHomeworkIds);
                } else {
                    w.in(EduHomework::getId, targetedHomeworkIds);
                }
            }
        });
        wrapper.eq(EduHomework::getStatus, 1); // 只看已发布的
        wrapper.orderByDesc(EduHomework::getCreatedAt);

        Page<EduHomework> result = eduHomeworkMapper.selectPage(pageParam, wrapper);
        List<HomeworkVO> voList = result.getRecords().stream()
                .map(h -> convertToHomeworkVO(h, studentId, 1))
                .collect(Collectors.toList());

        // 按提交状态筛选
        if (statusFilter != null) {
            voList = voList.stream()
                    .filter(v -> v.getMySubmitStatus() != null && v.getMySubmitStatus().equals(statusFilter))
                    .collect(Collectors.toList());
        }

        return new PageResult<>(result.getTotal(), page, size, voList);
    }

    /**
     * 提交作业（学生）
     */
    @Transactional
    public void submitHomework(Long homeworkId, Long studentId, HomeworkSubmitRequest request) {
        EduHomework homework = eduHomeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }

        // 检查截止时间
        if (homework.getDeadline() != null && LocalDateTime.now().isAfter(homework.getDeadline())) {
            throw new BusinessException("已过截止时间，无法提交");
        }

        // 检查是否已提交
        EduHomeworkSubmit existing = eduHomeworkSubmitMapper.selectOne(
                new LambdaQueryWrapper<EduHomeworkSubmit>()
                        .eq(EduHomeworkSubmit::getHomeworkId, homeworkId)
                        .eq(EduHomeworkSubmit::getStudentId, studentId)
        );

        if (existing != null) {
            if (existing.getStatus() != 2) { // 非退回状态不可重新提交
                throw new BusinessException("已提交，请勿重复提交");
            }
            // 退回后重新提交
            existing.setContent(request.getContent());
            existing.setAttachmentUrls(request.getAttachmentUrls());
            existing.setStatus(0);
            existing.setSubmitTime(LocalDateTime.now());
            existing.setScore(null);
            existing.setComment(null);
            eduHomeworkSubmitMapper.updateById(existing);
        } else {
            EduHomeworkSubmit submit = new EduHomeworkSubmit();
            submit.setHomeworkId(homeworkId);
            submit.setStudentId(studentId);
            submit.setContent(request.getContent());
            submit.setAttachmentUrls(request.getAttachmentUrls());
            submit.setStatus(0);
            submit.setSubmitTime(LocalDateTime.now());
            eduHomeworkSubmitMapper.insert(submit);
        }
    }

    /**
     * 获取作业提交列表（教师查看）
     */
    public List<HomeworkSubmitVO> getHomeworkSubmits(Long homeworkId, Integer status) {
        LambdaQueryWrapper<EduHomeworkSubmit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduHomeworkSubmit::getHomeworkId, homeworkId);
        if (status != null) {
            wrapper.eq(EduHomeworkSubmit::getStatus, status);
        }
        wrapper.orderByAsc(EduHomeworkSubmit::getSubmitTime);

        List<EduHomeworkSubmit> submits = eduHomeworkSubmitMapper.selectList(wrapper);
        EduHomework homework = eduHomeworkMapper.selectById(homeworkId);

        return submits.stream().map(s -> convertToSubmitVO(s, homework)).collect(Collectors.toList());
    }

    /**
     * 批改作业（教师）
     */
    @Transactional
    public void reviewHomework(Long submitId, HomeworkReviewRequest request) {
        EduHomeworkSubmit submit = eduHomeworkSubmitMapper.selectById(submitId);
        if (submit == null) {
            throw new BusinessException("提交记录不存在");
        }

        submit.setScore(request.getScore());
        submit.setComment(request.getComment());
        submit.setStatus(request.getStatus());
        submit.setReviewTime(LocalDateTime.now());
        eduHomeworkSubmitMapper.updateById(submit);
    }

    /**
     * 获取学生提交详情
     */
    public HomeworkSubmitVO getSubmitDetail(Long submitId) {
        EduHomeworkSubmit submit = eduHomeworkSubmitMapper.selectById(submitId);
        if (submit == null) {
            throw new BusinessException("提交记录不存在");
        }
        EduHomework homework = eduHomeworkMapper.selectById(submit.getHomeworkId());
        return convertToSubmitVO(submit, homework);
    }

    private HomeworkVO convertToHomeworkVO(EduHomework homework, Long currentUserId, Integer role) {
        HomeworkVO vo = new HomeworkVO();
        BeanUtils.copyProperties(homework, vo);

        // 班级名
        EduClass eduClass = eduClassMapper.selectById(homework.getClassId());
        if (eduClass != null) {
            vo.setClassName(eduClass.getName());
        }

        // 教师名
        SysUser teacher = sysUserMapper.selectById(homework.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getName());
        }

        // 提交统计
        Long submitCount = eduHomeworkSubmitMapper.selectCount(
                new LambdaQueryWrapper<EduHomeworkSubmit>().eq(EduHomeworkSubmit::getHomeworkId, homework.getId())
        );
        Long reviewedCount = eduHomeworkSubmitMapper.selectCount(
                new LambdaQueryWrapper<EduHomeworkSubmit>()
                        .eq(EduHomeworkSubmit::getHomeworkId, homework.getId())
                        .eq(EduHomeworkSubmit::getStatus, 1)
        );

        // 班级学生总数（或指定学生数）
        long totalCount;
        if (homework.getTargetType() != null && homework.getTargetType() == 1) {
            // 指定学生：查关联表
            totalCount = eduHomeworkStudentMapper.selectCount(
                    new LambdaQueryWrapper<EduHomeworkStudent>().eq(EduHomeworkStudent::getHomeworkId, homework.getId())
            );
        } else {
            // 全班：查班级学生表
            totalCount = eduClassStudentMapper.selectCount(
                    new LambdaQueryWrapper<EduClassStudent>().eq(EduClassStudent::getClassId, homework.getClassId())
            );
        }

        vo.setTotalCount((int) totalCount);
        vo.setSubmitCount(submitCount.intValue());
        vo.setReviewedCount(reviewedCount.intValue());

        // 教师视角且指定学生：返回学生ID和名字
        if (role == 2 && homework.getTargetType() != null && homework.getTargetType() == 1) {
            List<EduHomeworkStudent> hwStudents = eduHomeworkStudentMapper.selectList(
                new LambdaQueryWrapper<EduHomeworkStudent>().eq(EduHomeworkStudent::getHomeworkId, homework.getId())
            );
            List<Long> studentIds = hwStudents.stream().map(EduHomeworkStudent::getStudentId).collect(Collectors.toList());
            vo.setTargetStudentIds(studentIds);
            if (!studentIds.isEmpty()) {
                List<SysUser> students = sysUserMapper.selectBatchIds(studentIds);
                vo.setTargetStudentNames(students.stream().map(SysUser::getName).collect(Collectors.toList()));
            } else {
                vo.setTargetStudentNames(List.of());
            }
        }

        // 学生视角：获取我的提交状态
        if (role == 1) {
            EduHomeworkSubmit mySubmit = eduHomeworkSubmitMapper.selectOne(
                    new LambdaQueryWrapper<EduHomeworkSubmit>()
                            .eq(EduHomeworkSubmit::getHomeworkId, homework.getId())
                            .eq(EduHomeworkSubmit::getStudentId, currentUserId)
            );
            if (mySubmit == null) {
                vo.setMySubmitStatus(0); // 未提交
            } else {
                vo.setMySubmitStatus(mySubmit.getStatus() + 1); // 0->1(已提交), 1->2(已批改), 2->3(已退回)
                vo.setMySubmitId(String.valueOf(mySubmit.getId()));
            }
        }

        return vo;
    }

    private HomeworkSubmitVO convertToSubmitVO(EduHomeworkSubmit submit, EduHomework homework) {
        HomeworkSubmitVO vo = new HomeworkSubmitVO();
        BeanUtils.copyProperties(submit, vo);

        if (homework != null) {
            vo.setHomeworkTitle(homework.getTitle());
        }

        // 学生姓名
        SysUser student = sysUserMapper.selectById(submit.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getName());
        }

        // 状态名称
        switch (submit.getStatus()) {
            case 0 -> vo.setStatusName("已提交");
            case 1 -> vo.setStatusName("已批改");
            case 2 -> vo.setStatusName("已退回");
        }

        return vo;
    }
}
