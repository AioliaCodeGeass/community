package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.HostUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

import static com.nowcoder.community.util.CommunityConstant.*;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/30
 */
@Controller
@RequestMapping("/comment")
public class CommentController
{
    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(value="/add/{discussPostId}",method= RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Model model, Comment comment )
    {
        User user= HostUtil.getUser();
        comment.setUserId(user.getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        //触发评论事件
        Event event=new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(user.getId())
                .setEntityType(comment.getEntityType())
                .setEntityUserId(comment.getEntityId())
                .setData("postId",discussPostId);
        if(comment.getEntityType()==ENTITY_TYPE_POST)
        {
            DiscussPost target=discussPostService.getById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        else
        {
            Comment target =commentService.getById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);
        if(comment.getEntityType()==ENTITY_TYPE_POST)
        {
            event=new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(user.getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }

        return "redirect:/discuss/detail/"+discussPostId;
    }

    @RequestMapping(value="/comments/{userId}",method = RequestMethod.GET)
    public String getComments(@PathVariable("userId") int userId, Page page, Model model)
    {
        User user = userService.getById(userId);
        if (user == null)
        {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/comment/comments/" + userId);
        page.setRows((int) commentService.findCommentCountByUserId(userId));

        List<Comment> list= commentService.findCommentByUserId(userId,page.getCurrent(),page.getLimit());
        List<Map<String,Object>> comments=new ArrayList<>();
        if(list!=null)
        {
            for(Comment comment:list)
            {
                Map<String,Object> map=new HashMap<>();
                map.put("comment",comment);
                if(comment.getEntityType()==1)
                {
                    //查询帖子下的评论
                    DiscussPost post=discussPostService.getById(comment.getEntityId());
                    map.put("post",post);
                }
                else
                {
                    //查询评论的回复
                    Comment reply=commentService.getById(comment.getEntityId());
                    DiscussPost post=discussPostService.getById(reply.getEntityId());
                    map.put("post",post);
                }

                comments.add(map);
            }
        }
        model.addAttribute("comments",comments);
        model.addAttribute("commentCount",page.getRows());

        return "/site/my-reply";
    }
}
