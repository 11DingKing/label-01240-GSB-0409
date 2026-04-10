package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 博客实体
 */
@Data
public class Blog {
    private Long id;
    private String title;
    private String content;
    private String tags;
    private Long authorId;
    private Integer status;     // 0-草稿(DRAFT), 1-待初审(PENDING_REVIEW), 2-待终审(PENDING_FINAL), 3-已发布(PUBLISHED), 4-已拒绝(REJECTED), 5-已下架(TAKEN_DOWN)
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // 关联字段
    private String authorName;
    private String authorAvatar;
}
