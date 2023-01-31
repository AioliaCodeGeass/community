package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_POST;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/30
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService
{
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    @Override
    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit)
    {
        int pageNumber=offset;
        int pageSize=limit;
        Page<Comment> page=new Page<>(pageNumber,pageSize);
        LambdaQueryWrapper<Comment> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getStatus,0)
                .eq(Comment::getEntityType,entityType)
                .eq(Comment::getEntityId,entityId)
                .orderByAsc(Comment::getCreateTime);
        this.page(page,wrapper);
        return page.getRecords();
    }

    @Override
    public int findCommentCount(int entityType, int entityId)
    {
        LambdaQueryWrapper<Comment> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getStatus,0)
                .eq(Comment::getEntityType,entityType)
                .eq(Comment::getEntityId,entityId);
        int count=this.count(wrapper);
        return count;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment)
    {
        if(comment==null)
        {
            throw new IllegalArgumentException("参数不能为空！");
        }
        //添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        boolean isSuccess=save(comment);
        //更新帖子评论数量
        if(comment.getEntityType()==ENTITY_TYPE_POST)
        {
            int count=findCommentCount(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }

        return isSuccess==true?1:0;
    }
}
