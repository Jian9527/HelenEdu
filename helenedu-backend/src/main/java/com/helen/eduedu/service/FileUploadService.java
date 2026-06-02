package com.helen.eduedu.service;

import com.helen.eduedu.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Slf4j
@Service
public class FileUploadService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8888/uploads}")
    private String baseUrl;

    /**
     * 动态获取 base URL，优先使用当前请求的域名（便于手机访问）
     */
    private String getDynamicBaseUrl() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String host = request.getServerName();
                int port = request.getServerPort();
                String scheme = request.getScheme();
                String portStr = (port == 80 || port == 443) ? "" : ":" + port;
                return scheme + "://" + host + portStr + "/uploads";
            }
        } catch (Exception e) {
            log.debug("[文件上传] 无法获取请求域名, 使用默认baseUrl: {}", baseUrl);
        }
        return baseUrl;
    }

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * 上传单个文件
     */
    public String uploadFile(MultipartFile file) {
        log.debug("[文件上传] 开始上传: 原始文件名={}, 大小={}KB, 类型={}", 
                file.getOriginalFilename(), file.getSize() / 1024, file.getContentType());
        validateFile(file);

        try {
            // 按日期创建目录
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path dirPath = Paths.get(uploadDir, datePath);
            Files.createDirectories(dirPath);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 保存文件
            Path filePath = dirPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String url = getDynamicBaseUrl() + "/" + datePath + "/" + newFilename;
            // URL中附带原始文件名，便于前端展示
            if (originalFilename != null && !originalFilename.isEmpty()) {
                String encodedName = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
                url += "?name=" + encodedName;
            }
            log.debug("[文件上传] 上传成功: {}", url);
            return url;
        } catch (IOException e) {
            log.error("[文件上传] 上传失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 批量上传文件
     */
    public List<String> uploadFiles(List<MultipartFile> files) {
        log.debug("[文件上传] 批量上传: 文件数={}", files.size());
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file));
        }
        log.debug("[文件上传] 批量上传完成: {}个文件", urls.size());
        return urls;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过50MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("不支持的文件类型");
        }
    }
}
