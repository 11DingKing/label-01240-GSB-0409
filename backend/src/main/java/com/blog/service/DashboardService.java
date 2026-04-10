package com.blog.service;

import com.blog.mapper.BlogMapper;
import com.blog.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    @Cacheable(value = "dashboard", key = "'admin:dashboard'")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayNewBlogs", blogMapper.countTodayNewBlogs());
        stats.put("todayNewUsers", userMapper.countTodayNewUsers());
        stats.put("pendingReviewBlogs", blogMapper.countPendingReviewBlogs());
        stats.put("weeklyBlogTrends", blogMapper.countBlogsLast7Days());
        return stats;
    }
}