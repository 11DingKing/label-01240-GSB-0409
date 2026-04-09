package com.blog.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 博客实体
 */
@Data
public class Blog {
    // 博客状态常量
    public static final int STATUS_DRAFT = 0;           // 草稿
    public static final int STATUS_PENDING_REVIEW = 3;  // 待初审
    public static final int STATUS_PENDING_FINAL = 4;   // 待终审
    public static final int STATUS_PUBLISHED = 1;       // 已发布
    public static final int STATUS_REJECTED = 5;        // 已拒绝
    public static final int STATUS_OFFLINE = 2;         // 已下架
    
    private Long id;
    private String title;
    private String content;
    private String tags;
    private Long authorId;
    private Integer status;     // 0-草稿, 1-已发布, 2-已下架, 3-待初审, 4-待终审, 5-已拒绝
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // 关联字段
    private String authorName;
    private String authorAvatar;
    
    // 审核相关字段
    private String rejectReason;  // 拒绝原因
    private Long reviewerId;      // 审核人ID
    private String reviewerName;  // 审核人姓名
