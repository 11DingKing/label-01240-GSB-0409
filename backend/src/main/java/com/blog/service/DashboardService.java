package com.blog.service;

import com.blog.dto.DashboardStats;
import com.blog.mapper.BlogMapper;
import com.blog.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 看板服务
 */
@Service
public class DashboardService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private UserMapper userMapper;

    @Cacheable(value = "dashboard", key = "'admin:dashboard'")
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTodayNewBlogs(blogMapper.countTodayNewBlogs());
        stats.setTodayNewUsers(userMapper.countTodayNewUsers());
        stats.setPendingReviewBlogs(blogMapper.countPendingReviewBlogs());
        stats.setLast7DaysTrend(fillMissingDates(blogMapper.getLast7DaysPublishTrend()));
        return stats;
    }

    private List<Map<String, Object>> fillMissingDates(List<Map<String, Object>> trendData) {
        // 创建近7天的完整日期列表并补全缺失数据
        List<Map<String, Object>> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        
        // 已存在日期的数据映射
        Map<String, Long> dataMap = new HashMap<>();
        for (Map<String, Object> item : trendData) {
            dataMap.put(item.get("date").toString(), ((Number) item.get("count")).longValue());
        }
        
        // 从6天前到今天，共7天
        for (int i = 6; i >= 0; i--) {
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            String dateStr = String.format("%tF", cal.getTime());
            Long count = dataMap.getOrDefault(dateStr, 0L);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("count", count);
            result.add(dayData);
        }
        
        return result;
    }
}