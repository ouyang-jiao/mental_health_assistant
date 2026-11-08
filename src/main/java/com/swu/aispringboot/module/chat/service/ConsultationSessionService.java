package com.swu.aispringboot.module.chat.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.swu.aispringboot.module.chat.dto.ConsultationSessionCreateDTO;
import com.swu.aispringboot.module.chat.entity.ConsultationSession;
import com.swu.aispringboot.module.auth.entity.User;
import com.swu.aispringboot.module.chat.mapper.ConsultationSessionMapper;
import com.swu.aispringboot.module.auth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationSessionService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    //创建数据库会话记录
    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDTO consultationSessionCreateDTO) {
        //根据用户ID查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user != null) {
            //创建会话记录
            ConsultationSession consultationSession = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(consultationSessionCreateDTO.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();
            //如果未指定会话标题，默认使用当前时间
            if (StringUtils.isBlank(consultationSessionCreateDTO.getSessionTitle())) {
                consultationSession.setSessionTitle("AI助手 - " + DateUtil.format(LocalDateTime.now(),"yyyy-MM-dd HH:mm:ss"));
            }
            //保存会话记录
            consultationSessionMapper.insert(consultationSession);
            return consultationSession;
        }
        return null;
    }
}
