package com.blog.service;

import com.blog.dto.DashboardStatsDTO;
import com.blog.enums.BlogStatus;
import com.blog.mapper.BlogMapper;
import com.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.blog.config.CacheConfig.DASHBOARD_CACHE;

/**
 * 仪表盘服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BlogMapper blogMapper;
    private final UserMapper userMapper;

    /**
     * 获取仪表盘统计数据
     * 缓存30秒
     */
    @Cacheable(value = DASHBOARD_CACHE, key = "'stats'")
    public DashboardStatsDTO getDashboardStats() {
        log.debug("获取仪表盘统计数据（未命中缓存）");

        // 今日新增博客数
        Long todayNewBlogs = blogMapper.countTodayNewBlogs();

        // 今日新增用户数
        Long todayNewUsers = userMapper.countTodayNewUsers();

        // 待审核博客数（PENDING_REVIEW + PENDING_FINAL）
        Long pendingReviewCount = blogMapper.countByStatus(BlogStatus.PENDING_REVIEW.getCode())
                + blogMapper.countByStatus(BlogStatus.PENDING_FINAL.getCode());

        // 近7日每日发布博客趋势
        List<DashboardStatsDTO.DailyBlogTrend> last7DaysTrend = getLast7DaysTrend();

        return DashboardStatsDTO.builder()
                .todayNewBlogs(todayNewBlogs != null ? todayNewBlogs : 0L)
                .todayNewUsers(todayNewUsers != null ? todayNewUsers : 0L)
                .pendingReviewCount(pendingReviewCount != null ? pendingReviewCount : 0L)
                .last7DaysTrend(last7DaysTrend)
                .build();
    }

    /**
     * 获取近7日每日发布博客趋势
     */
    private List<DashboardStatsDTO.DailyBlogTrend> getLast7DaysTrend() {
        List<DashboardStatsDTO.DailyBlogTrend> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);

            // 查询当日发布的博客数（状态为已发布）
            Long count = blogMapper.countPublishedByDate(date.toString());

            trends.add(DashboardStatsDTO.DailyBlogTrend.builder()
                    .date(dateStr)
                    .count(count != null ? count : 0L)
                    .build());
        }

        return trends;
    }
}
