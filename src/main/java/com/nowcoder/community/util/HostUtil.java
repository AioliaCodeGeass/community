package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/28
 */
public class HostUtil
{
    private static ThreadLocal<User> users=new ThreadLocal<>();

    public static void setUser(User user)
    {
        users.set(user);
    }

    public static User getUser()
    {
        return users.get();
    }

    public static void clear()
    {
        users.remove();
    }
}
