package com.blog.enums;

import lombok.Getter;

/**
 * 博客状态枚举
 * 实现二级审核状态机：
 * DRAFT(0) -> PENDING_REVIEW(10) -> PENDING_FINAL(20) -> PUBLISHED(1)
 *                              \-> REJECTED(30)
 *                    \-> REJECTED(30)
 */
@Getter
public enum BlogStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    PENDING_REVIEW(10, "待初审"),
    PENDING_FINAL(20, "待终审"),
    REJECTED(30, "已拒绝");

    private final int code;
    private final String description;

    BlogStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BlogStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BlogStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查状态流转是否合法
     */
    public boolean canTransitionTo(BlogStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        return switch (this) {
            case DRAFT -> targetStatus == PENDING_REVIEW;
            case PENDING_REVIEW -> targetStatus == PENDING_FINAL || targetStatus == REJECTED;
            case PENDING_FINAL -> targetStatus == PUBLISHED || targetStatus == REJECTED;
            case PUBLISHED, REJECTED -> false;
        };
    }

    /**
     * 获取允许的目标状态列表
     */
    public BlogStatus[] getAllowedTransitions() {
        return switch (this) {
            case DRAFT -> new BlogStatus[]{PENDING_REVIEW};
            case PENDING_REVIEW -> new BlogStatus[]{PENDING_FINAL, REJECTED};
            case PENDING_FINAL -> new BlogStatus[]{PUBLISHED, REJECTED};
            case PUBLISHED, REJECTED -> new BlogStatus[]{};
        };
    }
}
