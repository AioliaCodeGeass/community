package com.nowcoder.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/28
 */
@Data
public class LoginTicket
{
    @TableId(value="id",type= IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String ticket;

    private Integer status;

    private Date expired;
}
