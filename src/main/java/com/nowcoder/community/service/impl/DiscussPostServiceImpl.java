package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dto.Result;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/26
 */
@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService
{
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

}
