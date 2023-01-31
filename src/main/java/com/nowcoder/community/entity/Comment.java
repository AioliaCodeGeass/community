package com.nowcoder.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/30
 */
@Data
public class Comment
{
    @TableId(value="id",type= IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer entityType;

    private Integer entityId;

    private Integer targetId;

    private String content;

    private Integer status;

    private Date createTime;
}
