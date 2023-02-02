package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/30
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant
{
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(value="/add",method= RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content)
    {
        User user= HostUtil.getUser();
        if(user==null)
        {
            return CommunityUtil.getJsonString(403,"你还没有登录！");
        }
        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setType(0);
        post.setStatus(0);
        post.setCreateTime(new Date());
        post.setCommentCount(0);
        post.setScore(0);
        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJsonString(0,"发布成功！");
    }

    @RequestMapping(value="/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page)
    {
        //帖子
        DiscussPost post=discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //作者
        User user=userService.getById(post.getUserId());
        model.addAttribute("user",user);
        //点赞数量
        long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus = HostUtil.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(HostUtil.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);
        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList=commentService.findCommentByEntity(ENTITY_TYPE_POST,post.getId(),page.getCurrent(),page.getLimit());
        //评论VO列表
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if(commentList!=null)
        {
            for(Comment comment:commentList)
            {
                //评论VO
                Map<String,Object> commentVo=new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //作者
                commentVo.put("user",userService.getById(comment.getUserId()));
                //点赞数量
                likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态
                likeStatus = HostUtil.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(HostUtil.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",likeStatus);
                //回复列表
                List<Comment> replyList=commentService.findCommentByEntity(ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String,Object>> replyVoList=new ArrayList<>();
                if(replyVoList!=null)
                {
                    for(Comment reply:replyList)
                    {
                        Map<String,Object> replyVo=new HashMap<>();
                        //回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",userService.getById(reply.getUserId()));
                        //回复目标
                        User target=reply.getTargetId()==0?null:userService.getById(reply.getTargetId());
                        replyVo.put("target",target);
                        //点赞数量
                        likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus = HostUtil.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(HostUtil.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                //回复数量
                int replyCount=commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }

    @RequestMapping(value="/discussPosts/{userId}",method = RequestMethod.GET)
    public String getDiscussPosts(@PathVariable("userId") int userId, Page page, Model model)
    {
        User user = userService.getById(userId);
        if (user == null)
        {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/discuss/discussPosts/" + userId);
        page.setRows((int) discussPostService.findDiscussPostRows(userId));

        List<DiscussPost> list= discussPostService.findDiscussPosts(userId,page.getCurrent(),page.getLimit());
        List<Map<String,Object>> discussPosts=new ArrayList<>();
        if(list!=null)
        {
            for(DiscussPost post:list)
            {
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("discussPostCount",page.getRows());

        return "/site/my-post";
    }
}
