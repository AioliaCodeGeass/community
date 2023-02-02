package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_USER;

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

    @Autowired
    private UserService userService;

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

    @RequestMapping(value="/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model)
    {
        User user = userService.getById(userId);
        if (user == null)
        {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null)
        {
            for (Map<String, Object> map : userList)
            {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);

        return "/site/followee";
    }

    @RequestMapping(value="/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model)
    {
        User user = userService.getById(userId);
        if (user == null)
        {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null)
        {
            for (Map<String, Object> map : userList)
            {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);

        return "/site/follower";
    }

    private boolean hasFollowed(int userId)
    {
        if(HostUtil.getUser()==null)
        {
            return false;
        }

        return followService.hasFollowed(HostUtil.getUser().getId(),ENTITY_TYPE_USER,userId);
    }

}
