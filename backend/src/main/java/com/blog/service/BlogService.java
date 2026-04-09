package com.blog.service;

import com.blog.dto.BlogRequest;
import com.blog.dto.PageRequest;
import com.blog.dto.PageResult;
import com.blog.entity.Blog;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogMapper;
import com.blog.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 博客服务
 */
@Slf4j
@Service
public class BlogService {

    @Autowired
    private BlogMapper blogMapper;

    /**
     * 获取博客列表（公开）
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public PageResult<Blog> getBlogList(PageRequest request) {
        List<Blog> list = blogMapper.findList(request.getKeyword(), request.getStatus(),
                request.getOffset(), request.getSize());
        Long total = blogMapper.countList(request.getKeyword(), request.getStatus());
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 获取博客详情
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Blog getBlogDetail(Long id) {
        Blog blog = blogMapper.findById(id);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        // 增加浏览次数
        blogMapper.incrementViewCount(id);
        blog.setViewCount(blog.getViewCount() + 1);

        return blog;
    }

    /**
     * 发布博客
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Blog createBlog(BlogRequest request) {
        Long userId = UserContext.getUserId();

        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setTags(request.getTags());
        blog.setAuthorId(userId);
        // 如果用户选择发布（status=1），则自动设置为待初审
        if (request.getStatus() == 1) {
            blog.setStatus(Blog.STATUS_PENDING_REVIEW);
        } else {
            blog.setStatus(request.getStatus());
        }
        blog.setViewCount(0);
        blog.setLikeCount(0);
        blog.setCommentCount(0);

        blogMapper.insert(blog);
        log.info("用户发布博客: userId={}, blogId={}, title={}, status={}", userId, blog.getId(), blog.getTitle(), blog.getStatus());

        return blogMapper.findById(blog.getId());
    }

    /**
     * 更新博客
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Blog updateBlog(Long id, BlogRequest request) {
        Long userId = UserContext.getUserId();
        Blog blog = blogMapper.findById(id);

        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        // 验证权限：只有作者或管理员可以编辑
        if (!blog.getAuthorId().equals(userId) && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限编辑此博客");
        }

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setTags(request.getTags());
        blog.setStatus(request.getStatus());

        blogMapper.update(blog);
        log.info("用户更新博客: userId={}, blogId={}", userId, id);

        return blogMapper.findById(id);
    }

    /**
     * 删除博客（软删除）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteBlog(Long id) {
        Long userId = UserContext.getUserId();
        Blog blog = blogMapper.findById(id);

        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        // 验证权限：只有作者或管理员可以删除
        if (!blog.getAuthorId().equals(userId) && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限删除此博客");
        }

        blogMapper.softDelete(id);
        log.info("用户删除博客: userId={}, blogId={}", userId, id);
    }

    /**
     * 批量删除博客（软删除）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void batchDeleteBlogs(List<Long> ids) {
        Long userId = UserContext.getUserId();
        boolean isAdmin = UserContext.isAdmin();

        for (Long id : ids) {
            Blog blog = blogMapper.findById(id);
            if (blog == null) {
                continue;
            }
            // 验证权限：只有作者或管理员可以删除
            if (!blog.getAuthorId().equals(userId) && !isAdmin) {
                throw new BusinessException(403, "无权限删除博客ID: " + id);
            }
        }

        blogMapper.batchSoftDelete(ids);
        log.info("用户批量删除博客: userId={}, ids={}", userId, ids);
    }

    /**
     * 获取用户博客列表
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public PageResult<Blog> getUserBlogs(Long userId, PageRequest request) {
        List<Blog> list = blogMapper.findByAuthorId(userId, 1, request.getOffset(), request.getSize());
        Long total = blogMapper.countByAuthorId(userId, 1);
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 获取我的博客列表
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public PageResult<Blog> getMyBlogs(PageRequest request) {
        Long userId = UserContext.getUserId();
        List<Blog> list = blogMapper.findByAuthorId(userId, request.getStatus(),
                request.getOffset(), request.getSize());
        Long total = blogMapper.countByAuthorId(userId, request.getStatus());
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 获取全站博客列表（管理员）
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public PageResult<Blog> getAllBlogs(PageRequest request) {
        List<Blog> list = blogMapper.findAllList(request.getKeyword(), request.getStatus(),
                request.getOffset(), request.getSize());
        Long total = blogMapper.countAllList(request.getKeyword(), request.getStatus());
        return PageResult.of(list, total, request.getPage(), request.getSize());
    }

    /**
     * 更新博客状态（管理员）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateBlogStatus(Long id, Integer status) {
        Blog blog = blogMapper.findById(id);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }

        blogMapper.updateStatus(id, status);
        log.info("管理员更新博客状态: blogId={}, status={}", id, status);
    }

    /**
     * 更新博客状态（带审核原因）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateBlogStatusWithReason(Long id, Integer status, String reason, Long reviewerId) {
        Blog blog = blogMapper.findById(id);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }
        
        // 状态机校验：只允许合法的状态流转
        validateStatusTransition(blog.getStatus(), status);

        blogMapper.updateStatusWithReason(id, status, reason, reviewerId);
        log.info("管理员审核博客: blogId={}, 从状态{}变为{}, reason={}", id, blog.getStatus(), status, reason);
    }
    
    /**
     * 校验状态流转合法性
     * PENDING_REVIEW(3) → PENDING_FINAL(4) → PUBLISHED(1)
     * 任意审核状态 → REJECTED(5)
     */
    private void validateStatusTransition(int fromStatus, int toStatus) {
        // 允许直接变为草稿或下架
        if (toStatus == Blog.STATUS_DRAFT || toStatus == Blog.STATUS_OFFLINE) {
            return;
        }
        
        // 拒绝状态：可从任意待审核状态变为拒绝
        if (toStatus == Blog.STATUS_REJECTED) {
            if (fromStatus != Blog.STATUS_PENDING_REVIEW && fromStatus != Blog.STATUS_PENDING_FINAL) {
                throw new BusinessException("只有待审核状态的博客才能被拒绝");
            }
            return;
        }
        
        // 正常审核流程
        if (fromStatus == Blog.STATUS_PENDING_REVIEW) {
            if (toStatus != Blog.STATUS_PENDING_FINAL) {
                throw new BusinessException("待初审状态只能变更为待终审(初审通过)或已拒绝");
            }
        } else if (fromStatus == Blog.STATUS_PENDING_FINAL) {
            if (toStatus != Blog.STATUS_PUBLISHED) {
                throw new BusinessException("待终审状态只能变更为已发布(终审通过)或已拒绝");
            }
        } else {
            throw new BusinessException("当前状态不允许进行审核操作");
        }
    }
}
