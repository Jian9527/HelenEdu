package com.helen.eduedu.dto;

import lombok.Data;

import java.util.List;

/**
 * 提交作业请求
 */
@Data
public class HomeworkSubmitRequest {
    private String content;
    private List<String> attachmentUrls;
}
