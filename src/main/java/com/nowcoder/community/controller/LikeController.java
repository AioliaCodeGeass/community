package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
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

import static com.nowcoder.community.util.CommunityConstant.TOPIC_LIKE;

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

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(value="/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId)
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
        //触发点赞事件
        if(likeStatus==1)
        {
            Event event=new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(HostUtil.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJsonString(0,null,map);
    }
}
