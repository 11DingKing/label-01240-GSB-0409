package com.blog.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 看板统计数据
 */
@Data
public class DashboardStats {
    private long todayNewBlogs;
    private long todayNewUsers;
    private long pendingReviewBlogs;
    private List<Map<String, Object>> last7DaysTrend;
}