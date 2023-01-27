package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/25
 */
@Data
public class User
{
    private Integer id;

    private String username;

    private String password;

    private String salt;

    private String email;

    private int type;

    private int status;

    private String activationCode;

    private String headerUrl;

    private Date createTime;
}
