package com.nowcoder.community.service;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/2/1
 */
public interface LikeService
{
    //点赞
    public void like(int userId,int entityType,int entityId,int entityUserId);

    //查询某实体点赞的数量
    public long findEntityLikeCount(int entityType,int entityId);

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId);

    //查询某个用户获得的赞
    public int findUserLikeCount(int userId);
}
