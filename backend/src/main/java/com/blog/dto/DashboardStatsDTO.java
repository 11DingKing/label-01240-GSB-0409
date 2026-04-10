package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 仪表盘统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    /**
     * 今日新增博客数
     */
    private Long todayNewBlogs;

    /**
     * 今日新增用户数
     */
    private Long todayNewUsers;

    /**
     * 待审核博客数（PENDING_REVIEW + PENDING_FINAL）
     */
    private Long pendingReviewCount;

    /**
     * 近7日每日发布博客趋势
     */
    private List<DailyBlogTrend> last7DaysTrend;

    /**
     * 每日博客趋势数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyBlogTrend {
        /**
         * 日期（格式：MM-dd）
         */
        private String date;

        /**
         * 当日发布博客数
         */
        private Long count;
    }
}
