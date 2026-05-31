package com.helen.eduedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helen.eduedu.common.BusinessException;
import com.helen.eduedu.common.PageResult;
import com.helen.eduedu.common.RoleEnum;
import com.helen.eduedu.dto.UserRequest;
import com.helen.eduedu.entity.SysUser;
import com.helen.eduedu.entity.EduClass;
import com.helen.eduedu.entity.EduClassStudent;
import com.helen.eduedu.mapper.EduClassMapper;
import com.helen.eduedu.mapper.EduClassStudentMapper;
import com.helen.eduedu.mapper.SysUserMapper;
import com.helen.eduedu.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final EduClassStudentMapper eduClassStudentMapper;
    private final EduClassMapper eduClassMapper;

    /**
     * 创建用户
     */
    @Transactional
    public Long createUser(UserRequest request) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(request, user);
        user.setStatus(1);
        sysUserMapper.insert(user);

        // 学生角色且指定了班级，自动加入班级
        if (request.getRole() != null && request.getRole() == 1 && request.getClassId() != null) {
            addStudentToClass(user.getId(), request.getClassId());
        }
        return user.getId();
    }

    /**
     * 更新用户
     */
    @Transactional
    public void updateUser(Long id, UserRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        BeanUtils.copyProperties(request, user);
        sysUserMapper.updateById(user);

        // 学生角色更新班级
        if (request.getRole() != null && request.getRole() == 1 && request.getClassId() != null) {
            // 先删除旧关联
            eduClassStudentMapper.delete(
                new LambdaQueryWrapper<EduClassStudent>().eq(EduClassStudent::getStudentId, id)
            );
            addStudentToClass(id, request.getClassId());
        }
    }

    /**
     * 禁用/启用用户
     */
    @Transactional
    public void toggleUserStatus(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        sysUserMapper.updateById(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        sysUserMapper.deleteById(id);
    }

    /**
     * 获取用户列表（分页）
     */
    public PageResult<UserVO> getUserList(int page, int size, Integer role, String keyword) {
        Page<SysUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getName, keyword).or().like(SysUser::getPhone, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);

        Page<SysUser> result = sysUserMapper.selectPage(pageParam, wrapper);
        List<UserVO> voList = result.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setRoleName(RoleEnum.fromCode(user.getRole()).getDesc());
            // 查询学生所属班级
            if (user.getRole() == 1) {
                EduClassStudent cs = eduClassStudentMapper.selectOne(
                    new LambdaQueryWrapper<EduClassStudent>().eq(EduClassStudent::getStudentId, user.getId()).last("LIMIT 1")
                );
                if (cs != null) {
                    vo.setClassId(cs.getClassId());
                    EduClass cls = eduClassMapper.selectById(cs.getClassId());
                    if (cls != null) {
                        vo.setClassName(cls.getName());
                    }
                }
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), page, size, voList);
    }

    /**
     * 获取所有教师列表（用于班级分配）
     */
    public List<UserVO> getAllTeachers() {
        List<SysUser> teachers = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, RoleEnum.TEACHER.getCode()).eq(SysUser::getStatus, 1)
        );
        return teachers.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setRoleName(RoleEnum.TEACHER.getDesc());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取所有学生列表（用于班级分配）
     */
    public List<UserVO> getAllStudents() {
        List<SysUser> students = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, RoleEnum.STUDENT.getCode()).eq(SysUser::getStatus, 1)
        );
        return students.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setRoleName(RoleEnum.STUDENT.getDesc());
            return vo;
        }).collect(Collectors.toList());
    }

    private void addStudentToClass(Long studentId, Long classId) {
        EduClassStudent existing = eduClassStudentMapper.selectOne(
            new LambdaQueryWrapper<EduClassStudent>()
                .eq(EduClassStudent::getClassId, classId)
                .eq(EduClassStudent::getStudentId, studentId)
        );
        if (existing == null) {
            EduClassStudent cs = new EduClassStudent();
            cs.setClassId(classId);
            cs.setStudentId(studentId);
            eduClassStudentMapper.insert(cs);
        }
    }
}
