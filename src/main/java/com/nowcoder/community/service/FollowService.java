package com.nowcoder.community.service;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/2/1
 */
public interface FollowService
{
    //关注
    public void follow(int userId,int entityType,int entityId);

    //取消关注
    public void unfollow(int userId,int entityType,int entityId);

    //查询关注的实体的数量
    public long findFolloweeCount(int userId,int entityType);

    //查询实体的粉丝的数量
    public long findFollowerCount(int entityType,int entityId);

    // 查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId);
}
