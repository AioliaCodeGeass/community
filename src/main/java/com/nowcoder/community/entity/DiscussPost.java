package com.nowcoder.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/26
 */
@Document(indexName="discusspost",type="_doc",shards=6,replicas =3 )
@Data
public class DiscussPost
{
    @Id
    @TableId(value="id",type= IdType.AUTO)
    private Integer id;

    @Field(type=FieldType.Integer)
    private Integer userId;

    @Field(type=FieldType.Text,analyzer = "ik_max_word",searchAnalyzer ="ik_smart" )
    private String title;

    @Field(type=FieldType.Text,analyzer = "ik_max_word",searchAnalyzer ="ik_smart" )
    private String content;

    @Field(type=FieldType.Integer)
    private Integer type;

    @Field(type=FieldType.Integer)
    private Integer status;

    @Field(type=FieldType.Date)
    private Date createTime;

    @Field(type=FieldType.Integer)
    private Integer commentCount;

    @Field(type=FieldType.Double)
    private double score;
}
