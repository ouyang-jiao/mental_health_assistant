package com.swu.aispringboot.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swu.aispringboot.module.chat.entity.ConsultationMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConsultationMessageMapper extends BaseMapper<ConsultationMessage> {
}
