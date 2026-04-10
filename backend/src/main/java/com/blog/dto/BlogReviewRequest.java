package com.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 博客审核请求DTO
 */
@Data
public class BlogReviewRequest {

    /**
     * 目标状态
     */
    @NotNull(message = "目标状态不能为空")
    private Integer status;

    /**
     * 审核原因（拒绝时必填）
     */
    private String reason;
}
