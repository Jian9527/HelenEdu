package com.helen.eduedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helen.eduedu.common.BusinessException;
import com.helen.eduedu.common.PageResult;
import com.helen.eduedu.dto.PreviewMaterialRequest;
import com.helen.eduedu.entity.EduClass;
import com.helen.eduedu.entity.EduClassStudent;
import com.helen.eduedu.entity.EduPreviewMaterial;
import com.helen.eduedu.entity.SysUser;
import com.helen.eduedu.mapper.EduClassMapper;
import com.helen.eduedu.mapper.EduClassStudentMapper;
import com.helen.eduedu.mapper.EduPreviewMaterialMapper;
import com.helen.eduedu.mapper.SysUserMapper;
import com.helen.eduedu.vo.PreviewMaterialVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 预习资料服务
 */
@Service
@RequiredArgsConstructor
public class PreviewMaterialService {

    private final EduPreviewMaterialMapper previewMaterialMapper;
    private final EduClassMapper eduClassMapper;
    private final EduClassStudentMapper eduClassStudentMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 发布预习资料
     */
    @Transactional
    public Long createMaterial(Long teacherId, PreviewMaterialRequest request) {
        EduPreviewMaterial material = new EduPreviewMaterial();
        BeanUtils.copyProperties(request, material);
        material.setTeacherId(teacherId);
        if (material.getStatus() == null) {
            material.setStatus(1);
        }
        previewMaterialMapper.insert(material);
        return material.getId();
    }

    /**
     * 更新预习资料
     */
    @Transactional
    public void updateMaterial(Long id, PreviewMaterialRequest request) {
        EduPreviewMaterial material = previewMaterialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException("资料不存在");
        }
        BeanUtils.copyProperties(request, material);
        previewMaterialMapper.updateById(material);
    }

    /**
     * 删除预习资料
     */
    @Transactional
    public void deleteMaterial(Long id) {
        previewMaterialMapper.deleteById(id);
    }

    /**
     * 获取预习资料详情
     */
    public PreviewMaterialVO getMaterialDetail(Long id) {
        EduPreviewMaterial material = previewMaterialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException("资料不存在");
        }
        return convertToVO(material);
    }

    /**
     * 教师获取预习资料列表
     */
    public PageResult<PreviewMaterialVO> getTeacherMaterialList(Long teacherId, Long classId, int page, int size) {
        Page<EduPreviewMaterial> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<EduPreviewMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduPreviewMaterial::getTeacherId, teacherId);
        if (classId != null) {
            wrapper.eq(EduPreviewMaterial::getClassId, classId);
        }
        wrapper.orderByDesc(EduPreviewMaterial::getCreatedAt);

        Page<EduPreviewMaterial> result = previewMaterialMapper.selectPage(pageParam, wrapper);
        List<PreviewMaterialVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), page, size, voList);
    }

    /**
     * 学生获取预习资料列表
     */
    public PageResult<PreviewMaterialVO> getStudentMaterialList(Long studentId, int page, int size) {
        // 获取学生所在班级
        List<EduClassStudent> relations = eduClassStudentMapper.selectList(
                new LambdaQueryWrapper<EduClassStudent>().eq(EduClassStudent::getStudentId, studentId)
        );
        List<Long> classIds = relations.stream().map(EduClassStudent::getClassId).collect(Collectors.toList());

        if (classIds.isEmpty()) {
            return new PageResult<>(0, page, size, List.of());
        }

        Page<EduPreviewMaterial> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<EduPreviewMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(EduPreviewMaterial::getClassId, classIds);
        wrapper.eq(EduPreviewMaterial::getStatus, 1); // 只看已发布的
        wrapper.orderByDesc(EduPreviewMaterial::getCreatedAt);

        Page<EduPreviewMaterial> result = previewMaterialMapper.selectPage(pageParam, wrapper);
        List<PreviewMaterialVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), page, size, voList);
    }

    private PreviewMaterialVO convertToVO(EduPreviewMaterial material) {
        PreviewMaterialVO vo = new PreviewMaterialVO();
        BeanUtils.copyProperties(material, vo);

        // 班级名
        EduClass eduClass = eduClassMapper.selectById(material.getClassId());
        if (eduClass != null) {
            vo.setClassName(eduClass.getName());
        }

        // 教师名
        SysUser teacher = sysUserMapper.selectById(material.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getName());
        }

        return vo;
    }
}
