package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/2/1
 */
@Controller
public class FollowController
{
    @Autowired
    private FollowService followService;

    @RequestMapping(value="/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId)
    {
        User user= HostUtil.getUser();
        if(user==null)
        {
            return CommunityUtil.getJsonString(1,"用户未登录！");
        }
        followService.follow(user.getId(),entityType,entityId);
        return CommunityUtil.getJsonString(0,"已关注！");
    }

    @RequestMapping(value="/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId)
    {
        User user= HostUtil.getUser();
        if(user==null)
        {
            return CommunityUtil.getJsonString(1,"用户未登录！");
        }
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJsonString(0,"已取消关注！");
    }
}
