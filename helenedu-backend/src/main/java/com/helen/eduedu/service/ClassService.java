package com.helen.eduedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helen.eduedu.common.BusinessException;
import com.helen.eduedu.common.PageResult;
import com.helen.eduedu.dto.ClassMemberRequest;
import com.helen.eduedu.dto.ClassRequest;
import com.helen.eduedu.entity.*;
import com.helen.eduedu.mapper.*;
import com.helen.eduedu.vo.ClassVO;
import com.helen.eduedu.vo.UserVO;
import com.helen.eduedu.common.RoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassService {

    private final EduClassMapper eduClassMapper;
    private final EduClassStudentMapper eduClassStudentMapper;
    private final EduClassTeacherMapper eduClassTeacherMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 创建班级
     */
    @Transactional
    public Long createClass(ClassRequest request) {
        log.debug("[班级管理] 创建班级: name={}, teacherId={}", request.getName(), request.getTeacherId());
        EduClass eduClass = new EduClass();
        BeanUtils.copyProperties(request, eduClass);
        eduClass.setStatus(1);
        eduClassMapper.insert(eduClass);
        log.debug("[班级管理] 班级创建成功: classId={}", eduClass.getId());

        // 如果指定了班主任，自动加入班级教师关联表
        if (request.getTeacherId() != null) {
            addTeacherIfAbsent(eduClass.getId(), request.getTeacherId());
        }
        return eduClass.getId();
    }

    /**
     * 更新班级
     */
    @Transactional
    public void updateClass(Long id, ClassRequest request) {
        log.debug("[班级管理] 更新班级: classId={}, name={}", id, request.getName());
        EduClass eduClass = eduClassMapper.selectById(id);
        if (eduClass == null) {
            throw new BusinessException("班级不存在");
        }
        Long oldTeacherId = eduClass.getTeacherId();
        BeanUtils.copyProperties(request, eduClass);
        eduClassMapper.updateById(eduClass);

        // 如果班主任变更，自动加入班级教师关联表
        Long newTeacherId = request.getTeacherId();
        if (newTeacherId != null && !newTeacherId.equals(oldTeacherId)) {
            addTeacherIfAbsent(id, newTeacherId);
            log.debug("[班级管理] 班主任变更: classId={}, newTeacherId={}", id, newTeacherId);
        }
        log.debug("[班级管理] 班级更新成功: classId={}", id);
    }

    /**
     * 如果教师不在班级中，自动添加
     */
    private void addTeacherIfAbsent(Long classId, Long teacherId) {
        EduClassTeacher existing = eduClassTeacherMapper.selectOne(
                new LambdaQueryWrapper<EduClassTeacher>()
                        .eq(EduClassTeacher::getClassId, classId)
                        .eq(EduClassTeacher::getTeacherId, teacherId)
        );
        if (existing == null) {
            EduClassTeacher ct = new EduClassTeacher();
            ct.setClassId(classId);
            ct.setTeacherId(teacherId);
            eduClassTeacherMapper.insert(ct);
        }
    }

    /**
     * 删除/解散班级
     */
    @Transactional
    public void deleteClass(Long id) {
        log.debug("[班级管理] 删除班级: classId={}", id);
        EduClass eduClass = eduClassMapper.selectById(id);
        if (eduClass == null) {
            throw new BusinessException("班级不存在");
        }
        // 软删除：设置状态为0
        eduClass.setStatus(0);
        eduClassMapper.updateById(eduClass);
        log.debug("[班级管理] 班级删除成功: classId={}", id);
    }

    /**
     * 获取班级列表（分页）
     */
    public PageResult<ClassVO> getClassList(int page, int size, String keyword) {
        Page<EduClass> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<EduClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduClass::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(EduClass::getName, keyword);
        }
        wrapper.orderByDesc(EduClass::getCreatedAt);

        Page<EduClass> result = eduClassMapper.selectPage(pageParam, wrapper);

        List<ClassVO> voList = result.getRecords().stream()
                .map(this::convertToClassVO)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), page, size, voList);
    }

    /**
     * 获取班级详情
     */
    public ClassVO getClassDetail(Long id) {
        EduClass eduClass = eduClassMapper.selectById(id);
        if (eduClass == null) {
            throw new BusinessException("班级不存在");
        }
        return convertToClassVO(eduClass);
    }

    /**
     * 获取班级学生列表
     */
    public List<UserVO> getClassStudents(Long classId) {
        List<EduClassStudent> students = eduClassStudentMapper.selectList(
                new LambdaQueryWrapper<EduClassStudent>().eq(EduClassStudent::getClassId, classId)
        );

        return students.stream().map(s -> {
            SysUser user = sysUserMapper.selectById(s.getStudentId());
            if (user == null) return null;
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setRoleName(RoleEnum.STUDENT.getDesc());
            return vo;
        }).filter(v -> v != null).collect(Collectors.toList());
    }

    /**
     * 添加学生到班级
     */
    @Transactional
    public void addStudentToClass(Long classId, ClassMemberRequest request) {
        // 检查是否已存在
        EduClassStudent existing = eduClassStudentMapper.selectOne(
                new LambdaQueryWrapper<EduClassStudent>()
                        .eq(EduClassStudent::getClassId, classId)
                        .eq(EduClassStudent::getStudentId, request.getUserId())
        );
        if (existing != null) {
            throw new BusinessException("该学生已在班级中");
        }

        EduClassStudent classStudent = new EduClassStudent();
        classStudent.setClassId(classId);
        classStudent.setStudentId(request.getUserId());
        eduClassStudentMapper.insert(classStudent);
    }

    /**
     * 从班级移除学生
     */
    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId) {
        eduClassStudentMapper.delete(
                new LambdaQueryWrapper<EduClassStudent>()
                        .eq(EduClassStudent::getClassId, classId)
                        .eq(EduClassStudent::getStudentId, studentId)
        );
    }

    /**
     * 获取班级教师列表
     */
    public List<UserVO> getClassTeachers(Long classId) {
        List<EduClassTeacher> teachers = eduClassTeacherMapper.selectList(
                new LambdaQueryWrapper<EduClassTeacher>().eq(EduClassTeacher::getClassId, classId)
        );

        return teachers.stream().map(t -> {
            SysUser user = sysUserMapper.selectById(t.getTeacherId());
            if (user == null) return null;
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setRoleName(RoleEnum.TEACHER.getDesc());
            return vo;
        }).filter(v -> v != null).collect(Collectors.toList());
    }

    /**
     * 添加教师到班级
     */
    @Transactional
    public void addTeacherToClass(Long classId, ClassMemberRequest request) {
        EduClassTeacher existing = eduClassTeacherMapper.selectOne(
                new LambdaQueryWrapper<EduClassTeacher>()
                        .eq(EduClassTeacher::getClassId, classId)
                        .eq(EduClassTeacher::getTeacherId, request.getUserId())
        );
        if (existing != null) {
            throw new BusinessException("该教师已在班级中");
        }

        EduClassTeacher classTeacher = new EduClassTeacher();
        classTeacher.setClassId(classId);
        classTeacher.setTeacherId(request.getUserId());
        classTeacher.setSubject(request.getSubject());
        eduClassTeacherMapper.insert(classTeacher);
    }

    /**
     * 从班级移除教师
     */
    @Transactional
    public void removeTeacherFromClass(Long classId, Long teacherId) {
        eduClassTeacherMapper.delete(
                new LambdaQueryWrapper<EduClassTeacher>()
                        .eq(EduClassTeacher::getClassId, classId)
                        .eq(EduClassTeacher::getTeacherId, teacherId)
        );
    }

    /**
     * 获取教师所教的班级列表
     */
    public List<ClassVO> getTeacherClasses(Long teacherId) {
        List<EduClassTeacher> relations = eduClassTeacherMapper.selectList(
                new LambdaQueryWrapper<EduClassTeacher>().eq(EduClassTeacher::getTeacherId, teacherId)
        );
        return relations.stream().map(r -> {
            EduClass eduClass = eduClassMapper.selectById(r.getClassId());
            if (eduClass == null || eduClass.getStatus() == 0) return null;
            return convertToClassVO(eduClass);
        }).filter(v -> v != null).collect(Collectors.toList());
    }

    /**
     * 获取学生所在的班级列表
     */
    public List<ClassVO> getStudentClasses(Long studentId) {
        List<EduClassStudent> relations = eduClassStudentMapper.selectList(
                new LambdaQueryWrapper<EduClassStudent>().eq(EduClassStudent::getStudentId, studentId)
        );
        return relations.stream().map(r -> {
            EduClass eduClass = eduClassMapper.selectById(r.getClassId());
            if (eduClass == null || eduClass.getStatus() == 0) return null;
            return convertToClassVO(eduClass);
        }).filter(v -> v != null).collect(Collectors.toList());
    }

    private ClassVO convertToClassVO(EduClass eduClass) {
        ClassVO vo = new ClassVO();
        BeanUtils.copyProperties(eduClass, vo);

        // 查询班主任姓名
        if (eduClass.getTeacherId() != null) {
            SysUser teacher = sysUserMapper.selectById(eduClass.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getName());
            }
        }

        // 查询学生数
        Long studentCount = eduClassStudentMapper.selectCount(
                new LambdaQueryWrapper<EduClassStudent>().eq(EduClassStudent::getClassId, eduClass.getId())
        );
        vo.setStudentCount(studentCount.intValue());

        // 查询教师数
        Long teacherCount = eduClassTeacherMapper.selectCount(
                new LambdaQueryWrapper<EduClassTeacher>().eq(EduClassTeacher::getClassId, eduClass.getId())
        );
        vo.setTeacherCount(teacherCount.intValue());

        return vo;
    }
}
