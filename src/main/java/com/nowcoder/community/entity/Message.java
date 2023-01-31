package com.nowcoder.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/31
 */
@Data
public class Message
{
    @TableId(value="id",type= IdType.AUTO)
    private Integer id;

    private Integer fromId;

    private Integer toId;

    private String conversationId;

    private String content;

    private Integer status;

    private Date createTime;

}
