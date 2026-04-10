package com.blog.service;

import com.blog.dto.BlogReviewRequest;
import com.blog.entity.Blog;
import com.blog.entity.ReviewLog;
import com.blog.enums.BlogStatus;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogMapper;
import com.blog.mapper.ReviewLogMapper;
import com.blog.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 博客审核服务
 * 实现二级审核状态机
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogReviewService {

    private final BlogMapper blogMapper;
    private final ReviewLogMapper reviewLogMapper;

    /**
     * 审核博客
     * 状态流转：PENDING_REVIEW(10) -> PENDING_FINAL(20) -> PUBLISHED(1)
     *                     \-> REJECTED(30)
     *          PENDING_FINAL(20) -> PUBLISHED(1)
     *                     \-> REJECTED(30)
     *
     * @param blogId 博客ID
     * @param request 审核请求
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void reviewBlog(Long blogId, BlogReviewRequest request) {
        Blog blog = blogMapper.findById(blogId);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        BlogStatus currentStatus = BlogStatus.fromCode(blog.getStatus());
        BlogStatus targetStatus = BlogStatus.fromCode(request.getStatus());

        if (currentStatus == null) {
            throw new BusinessException("当前博客状态异常");
        }

        if (targetStatus == null) {
            throw new BusinessException("目标状态无效");
        }

        // 检查状态流转是否合法
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new BusinessException(
                    String.format("状态流转不合法：无法从 %s 变为 %s",
                            currentStatus.getDescription(),
                            targetStatus.getDescription())
            );
        }

        // 拒绝时必须提供原因
        if (targetStatus == BlogStatus.REJECTED) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new BusinessException("拒绝时必须提供原因");
            }
        }

        // 更新博客状态
        blogMapper.updateStatusWithReason(
                blogId,
                targetStatus.getCode(),
                request.getReason(),
                UserContext.getUserId()
        );

        // 记录审核日志
        String action = targetStatus == BlogStatus.REJECTED ? "reject" :
                (targetStatus == BlogStatus.PUBLISHED ? "approve_final" : "approve");

        ReviewLog reviewLog = new ReviewLog();
        reviewLog.setTargetType("blog");
        reviewLog.setTargetId(blogId);
        reviewLog.setAction(action);
        reviewLog.setReason(request.getReason());
        reviewLog.setReviewerId(UserContext.getUserId());
        reviewLog.setReviewerName(UserContext.getUsername());
        reviewLogMapper.insert(reviewLog);

        log.info("博客审核完成: blogId={}, currentStatus={}, targetStatus={}, reviewerId={}",
                blogId, currentStatus, targetStatus, UserContext.getUserId());
    }

    /**
     * 提交博客审核（用户操作：草稿 -> 待初审）
     *
     * @param blogId 博客ID
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void submitForReview(Long blogId) {
        Long userId = UserContext.getUserId();
        Blog blog = blogMapper.findById(blogId);

        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        // 验证权限：只有作者可以提交审核
        if (!blog.getAuthorId().equals(userId) && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限操作此博客");
        }

        BlogStatus currentStatus = BlogStatus.fromCode(blog.getStatus());

        if (currentStatus != BlogStatus.DRAFT && currentStatus != BlogStatus.REJECTED) {
            throw new BusinessException("只有草稿或被拒绝的博客可以提交审核");
        }

        // 更新为待初审状态
        blogMapper.updateStatus(blogId, BlogStatus.PENDING_REVIEW.getCode());

        log.info("用户提交博客审核: blogId={}, userId={}", blogId, userId);
    }
}
