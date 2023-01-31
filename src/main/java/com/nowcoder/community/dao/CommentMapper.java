package com.nowcoder.community.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/30
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment>
{

}
