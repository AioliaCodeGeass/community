package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/2/5
 */
public interface ElasticsearchService
{
    public void saveDiscussPost(DiscussPost post);

    public void deleteDiscussPost(int id);

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);

}
