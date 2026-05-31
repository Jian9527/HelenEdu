package com.helen.eduedu.controller;

import com.helen.eduedu.common.PageResult;
import com.helen.eduedu.common.R;
import com.helen.eduedu.dto.PreviewMaterialRequest;
import com.helen.eduedu.security.RequireRole;
import com.helen.eduedu.service.PreviewMaterialService;
import com.helen.eduedu.vo.PreviewMaterialVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 预习资料控制器
 */
@Tag(name = "预习资料")
@RestController
@RequestMapping("/api/preview")
@RequiredArgsConstructor
public class PreviewMaterialController {

    private final PreviewMaterialService previewMaterialService;

    @Operation(summary = "发布预习资料")
    @PostMapping
    @RequireRole({2})
    public R<Long> createMaterial(HttpServletRequest request, @Valid @RequestBody PreviewMaterialRequest req) {
        Long teacherId = (Long) request.getAttribute("userId");
        return R.ok(previewMaterialService.createMaterial(teacherId, req));
    }

    @Operation(summary = "更新预习资料")
    @PutMapping("/{id}")
    @RequireRole({2})
    public R<Void> updateMaterial(@PathVariable Long id, @Valid @RequestBody PreviewMaterialRequest req) {
        previewMaterialService.updateMaterial(id, req);
        return R.ok();
    }

    @Operation(summary = "删除预习资料")
    @DeleteMapping("/{id}")
    @RequireRole({2})
    public R<Void> deleteMaterial(@PathVariable Long id) {
        previewMaterialService.deleteMaterial(id);
        return R.ok();
    }

    @Operation(summary = "获取预习资料详情")
    @GetMapping("/{id}")
    public R<PreviewMaterialVO> getMaterialDetail(@PathVariable Long id) {
        return R.ok(previewMaterialService.getMaterialDetail(id));
    }

    @Operation(summary = "教师获取预习资料列表")
    @GetMapping("/list")
    @RequireRole({2})
    public R<PageResult<PreviewMaterialVO>> getTeacherMaterialList(
            HttpServletRequest request,
            @RequestParam(required = false) Long classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long teacherId = (Long) request.getAttribute("userId");
        return R.ok(previewMaterialService.getTeacherMaterialList(teacherId, classId, page, size));
    }

    @Operation(summary = "学生获取预习资料列表")
    @GetMapping("/student-list")
    @RequireRole({1})
    public R<PageResult<PreviewMaterialVO>> getStudentMaterialList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long studentId = (Long) request.getAttribute("userId");
        return R.ok(previewMaterialService.getStudentMaterialList(studentId, page, size));
    }
}
