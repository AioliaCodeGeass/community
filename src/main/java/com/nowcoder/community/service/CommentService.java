package com.nowcoder.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.entity.Comment;

import java.util.List;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/30
 */
public interface CommentService extends IService<Comment>
{
    public List<Comment> findCommentByEntity(int entityType,int entityId,int offset,int limit);

    public int findCommentCount(int entityType,int entityId);

    public int addComment(Comment comment);

    public int findCommentCountByUserId(int userId);

    public List<Comment> findCommentByUserId(int userId, int offset, int limit);

}
