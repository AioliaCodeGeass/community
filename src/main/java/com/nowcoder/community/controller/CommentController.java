package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.util.HostUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

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

    @RequestMapping(value="/add/{discussPostId}",method= RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Model model, Comment comment )
    {
        User user= HostUtil.getUser();
        comment.setUserId(user.getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        return "redirect:/discuss/detail/"+discussPostId;
    }

}
