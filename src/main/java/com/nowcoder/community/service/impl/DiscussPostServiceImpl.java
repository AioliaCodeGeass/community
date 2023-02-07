package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dto.Result;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/26
 */
@Slf4j
@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService
{
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //帖子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init()
    {
        //初始化帖子列表缓存
        postListCache= Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>()
                {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception
                    {
                        if(key==null||key.length()==0)
                        {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[]params=key.split(":");
                        if(params==null||params.length!=2)
                        {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset=Integer.valueOf(params[0]);
                        int limit=Integer.valueOf(params[1]);
                        log.debug("load post list from DB.");
                        return selectDiscussPosts(0,offset,limit,1);
                    }
                });
        //初始化帖子总数缓存
        postRowsCache=Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>()
                {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception
                    {
                        log.debug("load post rows from DB.");
                        return selectDiscussPostRows(key);
                    }
                });
    }

    private List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode)
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
                .orderByDesc(DiscussPost::getType);
        if(orderMode==1)
        {
            wrapper.orderByDesc(DiscussPost::getScore);
        }
        wrapper.orderByDesc(DiscussPost::getCreateTime);
        this.page(page,wrapper);
        return page.getRecords();
    }

    private int selectDiscussPostRows(int userId)
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


    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode)
    {
        if(userId==0&&orderMode==1)
        {
            return postListCache.get(offset+":"+limit);
        }
        log.debug("load post list from DB.");

        int pageNumber=offset;
        int pageSize=limit;
        Page<DiscussPost> page=new Page<>(pageNumber,pageSize);
        LambdaQueryWrapper<DiscussPost> wrapper=new LambdaQueryWrapper<>();
        if(userId!=0)
        {
            wrapper.eq(DiscussPost::getUserId,userId);
        }
        wrapper.ne(DiscussPost::getStatus,2)
                .orderByDesc(DiscussPost::getType);
        if(orderMode==1)
        {
            wrapper.orderByDesc(DiscussPost::getScore);
        }
        wrapper.orderByDesc(DiscussPost::getCreateTime);
        this.page(page,wrapper);
        return page.getRecords();
    }

    @Override
    public int findDiscussPostRows(int userId)
    {
        if(userId==0)
        {
            return postRowsCache.get(userId);
        }
        log.debug("load post rows from DB.");

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
