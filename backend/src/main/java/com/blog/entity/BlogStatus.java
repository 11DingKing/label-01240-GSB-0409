package com.blog.entity;

import lombok.Getter;

@Getter
public enum BlogStatus {
    DRAFT(0, "草稿"),
    PENDING_REVIEW(1, "待初审"),
    PENDING_FINAL(2, "待终审"),
    PUBLISHED(3, "已发布"),
    REJECTED(4, "已拒绝"),
    TAKEN_DOWN(5, "已下架");

    private final int value;
    private final String description;

    BlogStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static BlogStatus fromValue(int value) {
        for (BlogStatus status : BlogStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid blog status value: " + value);
    }

    public boolean canTransitionTo(BlogStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == PENDING_REVIEW;
            case PENDING_REVIEW -> newStatus == PENDING_FINAL || newStatus == REJECTED;
            case PENDING_FINAL -> newStatus == PUBLISHED || newStatus == REJECTED;
            case PUBLISHED -> newStatus == TAKEN_DOWN;
            case TAKEN_DOWN -> newStatus == PUBLISHED;
            case REJECTED -> newStatus == PENDING_REVIEW;
        };
    }
}