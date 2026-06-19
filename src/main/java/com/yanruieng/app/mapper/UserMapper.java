package com.yanruieng.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanruieng.app.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
