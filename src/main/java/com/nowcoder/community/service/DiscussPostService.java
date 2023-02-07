package com.nowcoder.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.dto.Result;
import com.nowcoder.community.entity.DiscussPost;

import java.util.List;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/26
 */
public interface DiscussPostService extends IService<DiscussPost>
{
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode);

    public int findDiscussPostRows(int userId);

    public int addDiscussPost(DiscussPost post);

    public DiscussPost findDiscussPostById(int id);

    public int updateCommentCount(int id,int commentCount);
}
