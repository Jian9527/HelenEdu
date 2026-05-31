package com.helen.eduedu.controller;

import com.helen.eduedu.common.PageResult;
import com.helen.eduedu.common.R;
import com.helen.eduedu.dto.ClassMemberRequest;
import com.helen.eduedu.dto.ClassRequest;
import com.helen.eduedu.security.RequireRole;
import com.helen.eduedu.service.ClassService;
import com.helen.eduedu.vo.ClassVO;
import com.helen.eduedu.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级管理控制器
 */
@Tag(name = "班级管理")
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @Operation(summary = "创建班级")
    @PostMapping
    @RequireRole({3})
    public R<Long> createClass(@Valid @RequestBody ClassRequest request) {
        return R.ok(classService.createClass(request));
    }

    @Operation(summary = "更新班级")
    @PutMapping("/{id}")
    @RequireRole({3})
    public R<Void> updateClass(@PathVariable Long id, @Valid @RequestBody ClassRequest request) {
        classService.updateClass(id, request);
        return R.ok();
    }

    @Operation(summary = "删除/解散班级")
    @DeleteMapping("/{id}")
    @RequireRole({3})
    public R<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return R.ok();
    }

    @Operation(summary = "获取班级列表")
    @GetMapping("/list")
    public R<PageResult<ClassVO>> getClassList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(classService.getClassList(page, size, keyword));
    }

    @Operation(summary = "获取班级详情")
    @GetMapping("/{id}")
    public R<ClassVO> getClassDetail(@PathVariable Long id) {
        return R.ok(classService.getClassDetail(id));
    }

    @Operation(summary = "获取班级学生列表")
    @GetMapping("/{id}/students")
    public R<List<UserVO>> getClassStudents(@PathVariable Long id) {
        return R.ok(classService.getClassStudents(id));
    }

    @Operation(summary = "添加学生到班级")
    @PostMapping("/{id}/students")
    @RequireRole({2, 3})
    public R<Void> addStudentToClass(@PathVariable Long id, @Valid @RequestBody ClassMemberRequest request) {
        classService.addStudentToClass(id, request);
        return R.ok();
    }

    @Operation(summary = "从班级移除学生")
    @DeleteMapping("/{id}/students/{studentId}")
    @RequireRole({2, 3})
    public R<Void> removeStudentFromClass(@PathVariable Long id, @PathVariable Long studentId) {
        classService.removeStudentFromClass(id, studentId);
        return R.ok();
    }

    @Operation(summary = "获取班级教师列表")
    @GetMapping("/{id}/teachers")
    public R<List<UserVO>> getClassTeachers(@PathVariable Long id) {
        return R.ok(classService.getClassTeachers(id));
    }

    @Operation(summary = "添加教师到班级")
    @PostMapping("/{id}/teachers")
    @RequireRole({3})
    public R<Void> addTeacherToClass(@PathVariable Long id, @Valid @RequestBody ClassMemberRequest request) {
        classService.addTeacherToClass(id, request);
        return R.ok();
    }

    @Operation(summary = "从班级移除教师")
    @DeleteMapping("/{id}/teachers/{teacherId}")
    @RequireRole({3})
    public R<Void> removeTeacherFromClass(@PathVariable Long id, @PathVariable Long teacherId) {
        classService.removeTeacherFromClass(id, teacherId);
        return R.ok();
    }

    @Operation(summary = "获取我的班级列表（教师）")
    @GetMapping("/my-classes")
    @RequireRole({2})
    public R<List<ClassVO>> getMyClasses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(classService.getTeacherClasses(userId));
    }

    @Operation(summary = "获取我的班级列表（学生）")
    @GetMapping("/my-student-classes")
    @RequireRole({1})
    public R<List<ClassVO>> getMyStudentClasses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(classService.getStudentClasses(userId));
    }
}
