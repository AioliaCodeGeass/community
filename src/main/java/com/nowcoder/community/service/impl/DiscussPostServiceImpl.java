package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dto.Result;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/26
 */
@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService
{
    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit)
    {
        int pageNumber=offset;
        int pageSize=limit;
        Page<DiscussPost> page=new Page<>(pageNumber,pageSize);
        LambdaQueryWrapper<DiscussPost> wrapper=new LambdaQueryWrapper<>();
        if(userId!=0)
        {
            wrapper.eq(DiscussPost::getUserId,userId);
        }
        wrapper.ne(DiscussPost::getStatus,2)
                .orderByDesc(DiscussPost::getType)
                .orderByDesc(DiscussPost::getCreateTime);
        this.page(page,wrapper);
        return page.getRecords();
    }

    @Override
    public int findDiscussPostRows(int userId)
    {
        LambdaQueryWrapper<DiscussPost> wrapper=new LambdaQueryWrapper<>();
        if(userId!=0)
        {
            wrapper.eq(DiscussPost::getUserId,userId);
        }
        wrapper.ne(DiscussPost::getStatus,2);
        int count=this.count(wrapper);
        return count;
    }

    @Override
    public int addDiscussPost(DiscussPost post)
    {
        if(post==null)
        {
            throw new IllegalArgumentException("参数不能为空！");
        }
        //转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        //保存讨论帖
        boolean isSuccess= this.save(post);
        return isSuccess==true?1:0;
    }

    @Override
    public DiscussPost findDiscussPostById(int id)
    {
        return this.getById(id);
    }

    @Override
    public int updateCommentCount(int id, int commentCount)
    {
        DiscussPost post=new DiscussPost();
        post.setId(id);
        post.setCommentCount(commentCount);
        boolean isSuccess=this.updateById(post);
        return isSuccess==true?1:0;
    }
}
