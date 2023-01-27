package com.nowcoder.community.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/25
 */
@Mapper
public interface UserMapper extends BaseMapper<User>
{

}
