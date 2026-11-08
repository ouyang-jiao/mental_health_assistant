package com.swu.aispringboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swu.aispringboot.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
