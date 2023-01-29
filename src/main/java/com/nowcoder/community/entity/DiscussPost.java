package com.nowcoder.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/26
 */
@Data
public class DiscussPost
{
    @TableId(value="id",type= IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String title;

    private String content;

    private Integer type;

    private Integer status;

    private Date createTime;

    private Integer commentCount;

    private double score;
}
