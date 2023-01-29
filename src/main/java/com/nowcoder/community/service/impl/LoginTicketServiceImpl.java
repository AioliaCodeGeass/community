package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.service.LoginTicketService;
import org.springframework.stereotype.Service;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/28
 */
@Service
public class LoginTicketServiceImpl extends ServiceImpl<LoginTicketMapper, LoginTicket> implements LoginTicketService
{
}
