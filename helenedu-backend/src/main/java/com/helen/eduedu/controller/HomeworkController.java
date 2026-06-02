package com.helen.eduedu.controller;

import com.helen.eduedu.common.PageResult;
import com.helen.eduedu.common.R;
import com.helen.eduedu.dto.HomeworkRequest;
import com.helen.eduedu.dto.HomeworkReviewRequest;
import com.helen.eduedu.dto.HomeworkSubmitRequest;
import com.helen.eduedu.security.RequireRole;
import com.helen.eduedu.service.HomeworkService;
import com.helen.eduedu.vo.HomeworkSubmitVO;
import com.helen.eduedu.vo.HomeworkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业管理控制器
 */
@Slf4j
@Tag(name = "作业管理")
@RestController
@RequestMapping("/api/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;

    @Operation(summary = "布置作业")
    @PostMapping
    @RequireRole({2})
    public R<Long> createHomework(HttpServletRequest request, @Valid @RequestBody HomeworkRequest req) {
        Long teacherId = (Long) request.getAttribute("userId");
        log.debug("[作业API] 创建作业请求: teacherId={}, title={}, attachmentUrls={}", teacherId, req.getTitle(), req.getAttachmentUrls());
        return R.ok(homeworkService.createHomework(teacherId, req));
    }

    @Operation(summary = "更新作业")
    @PutMapping("/{id}")
    @RequireRole({2})
    public R<Void> updateHomework(@PathVariable Long id, @Valid @RequestBody HomeworkRequest req) {
        log.debug("[作业API] 更新作业请求: homeworkId={}, attachmentUrls={}", id, req.getAttachmentUrls());
        homeworkService.updateHomework(id, req);
        return R.ok();
    }

    @Operation(summary = "删除作业")
    @DeleteMapping("/{id}")
    @RequireRole({2})
    public R<Void> deleteHomework(@PathVariable Long id) {
        homeworkService.deleteHomework(id);
        return R.ok();
    }

    @Operation(summary = "获取作业详情")
    @GetMapping("/{id}")
    public R<HomeworkVO> getHomeworkDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        return R.ok(homeworkService.getHomeworkDetail(id, userId, role));
    }

    @Operation(summary = "教师获取作业列表")
    @GetMapping("/list")
    @RequireRole({2})
    public R<PageResult<HomeworkVO>> getTeacherHomeworkList(
            HttpServletRequest request,
            @RequestParam(required = false) Long classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long teacherId = (Long) request.getAttribute("userId");
        return R.ok(homeworkService.getTeacherHomeworkList(teacherId, classId, page, size));
    }

    @Operation(summary = "学生获取作业列表")
    @GetMapping("/student-list")
    @RequireRole({1})
    public R<PageResult<HomeworkVO>> getStudentHomeworkList(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long studentId = (Long) request.getAttribute("userId");
        return R.ok(homeworkService.getStudentHomeworkList(studentId, status, page, size));
    }

    @Operation(summary = "提交作业")
    @PostMapping("/{id}/submit")
    @RequireRole({1})
    public R<Void> submitHomework(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody HomeworkSubmitRequest req) {
        Long studentId = (Long) request.getAttribute("userId");
        homeworkService.submitHomework(id, studentId, req);
        return R.ok();
    }

    @Operation(summary = "获取作业提交列表")
    @GetMapping("/{id}/submits")
    @RequireRole({2})
    public R<List<HomeworkSubmitVO>> getHomeworkSubmits(
            @PathVariable Long id,
            @RequestParam(required = false) Integer status) {
        return R.ok(homeworkService.getHomeworkSubmits(id, status));
    }

    @Operation(summary = "批改作业")
    @PutMapping("/submit/{id}/review")
    @RequireRole({2})
    public R<Void> reviewHomework(@PathVariable Long id, @Valid @RequestBody HomeworkReviewRequest req) {
        homeworkService.reviewHomework(id, req);
        return R.ok();
    }

    @Operation(summary = "获取提交详情")
    @GetMapping("/submit/{id}")
    public R<HomeworkSubmitVO> getSubmitDetail(@PathVariable Long id) {
        return R.ok(homeworkService.getSubmitDetail(id));
    }
}
