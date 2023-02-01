package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/2/1
 */
@Controller
public class LikeController
{
    @Autowired
    private LikeService likeService;

    @RequestMapping(value="/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId)
    {
        User user= HostUtil.getUser();
        if(user==null)
        {
            return CommunityUtil.getJsonString(1,"用户未登录！");
        }
        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //数量
        long likeCount=likeService.findEntityLikeCount(entityType,entityId);
        //状态
        int likeStatus=likeService.findEntityLikeStatus(user.getId(),entityType,entityId);
        //返回的结果
        Map<String,Object> map=new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        return CommunityUtil.getJsonString(0,null,map);
    }
}
