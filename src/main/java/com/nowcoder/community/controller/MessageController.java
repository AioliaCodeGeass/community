package com.nowcoder.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
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

import java.util.*;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/31
 */
@Controller
public class MessageController
{
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @RequestMapping(value="/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page)
    {
        User user= HostUtil.getUser();
        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList=messageService.findConversations(user.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> converastions=new ArrayList<>();
        if(conversationList!=null)
        {
            for(Message message:conversationList)
            {
                Map<String,Object> map=new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId=user.getId()==message.getFromId()?message.getToId():message.getFromId();
                map.put("target",userService.getById(targetId));

                converastions.add(map);
            }
        }
        model.addAttribute("conversations",converastions);
        //查询维度消息数量
        int letterUnreadCount=messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(value="/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model)
    {
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        //私信列表
        List<Message> letterList=messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> letters=new ArrayList<>();
        if(letterList!=null)
        {
            for(Message message:letterList)
            {
                Map<String,Object> map=new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.getById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));
        //设置已读
        List<Integer> ids=getLetterIds(letterList);
        if(!ids.isEmpty())
        {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId)
    {
        String[] ids=conversationId.split("_");
        int id0=Integer.parseInt(ids[0]);
        int id1=Integer.parseInt(ids[1]);
        if(HostUtil.getUser().getId()==id0)
        {
            return userService.getById(id1);
        }
        else
        {
            return userService.getById(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList)
    {
        List<Integer> ids=new ArrayList<>();
        if(letterList!=null)
        {
            for(Message message:letterList)
            {
                if(HostUtil.getUser().getId().equals(message.getToId())&&message.getStatus()==0)
                {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @RequestMapping(value="/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content)
    {
        User target=userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername,toName));
        if(target==null)
        {
            return CommunityUtil.getJsonString(1,"目标用户不存在！");
        }
        Message message=new Message();
        message.setFromId(HostUtil.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId()<message.getToId())
        {
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }
        else
        {
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJsonString(0);
    }

    @RequestMapping(value="/letter/delete",method=RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(Integer id)
    {
        if(id==null)
        {
            return CommunityUtil.getJsonString(1,"未选择删除哪条信息！");
        }
        int rows=messageService.deleteMessage(id);
        if(rows==0)
        {
            return CommunityUtil.getJsonString(1,"该消息不存在！");
        }
        return CommunityUtil.getJsonString(0);
    }

}
