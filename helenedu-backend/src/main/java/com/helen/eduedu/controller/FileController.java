package com.helen.eduedu.controller;

import com.helen.eduedu.common.R;
import com.helen.eduedu.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    @Operation(summary = "上传单个文件")
    @PostMapping("/upload")
    public R<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return R.ok(fileUploadService.uploadFile(file));
    }

    @Operation(summary = "批量上传文件")
    @PostMapping("/upload-batch")
    public R<List<String>> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        return R.ok(fileUploadService.uploadFiles(files));
    }
}
