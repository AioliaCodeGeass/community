package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService
{
}
