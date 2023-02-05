package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/2/4
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer>
{
}
