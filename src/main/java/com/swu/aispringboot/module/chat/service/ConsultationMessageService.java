package com.swu.aispringboot.module.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swu.aispringboot.module.chat.enums.MessageType;
import com.swu.aispringboot.module.chat.enums.SenderType;
import com.swu.aispringboot.module.chat.dto.ConsultationMessageResponseDTO;
import com.swu.aispringboot.module.chat.entity.ConsultationMessage;
import com.swu.aispringboot.module.chat.mapper.ConsultationMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class ConsultationMessageService {
    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    public ConsultationMessage saveUserMessage(Long sessionId, String content, String emotion_tag) {
        //构建用户消息实体
        ConsultationMessage userMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(SenderType.USER.getCode())
                .messageType(MessageType.TEXT.getCode())
                .content(content)
                .emotionTag(emotion_tag)
                .createdAt(LocalDateTime.now())
                .build();
        consultationMessageMapper.insert(userMessage);
        return userMessage;
    }

    public ConsultationMessage saveAiMessage(Long sessionId, String content, String aiModel) {
        //构建AI消息实体
        ConsultationMessage aiMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(SenderType.AI.getCode())
                .messageType(MessageType.TEXT.getCode())
                .content(content)
                .aiModel(aiModel)
                .createdAt(LocalDateTime.now())
                .build();
        consultationMessageMapper.insert(aiMessage);
        return aiMessage;
    }

    public Integer getMessageCountBySessionId(Long sessionId) {
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId);
        return consultationMessageMapper.selectCount(queryWrapper).intValue();
    }

    //获取回话的最后一条消息
    public ConsultationMessageResponseDTO getLastMessageBySessionId(Long sessionId) {
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                    .orderByDesc(ConsultationMessage::getCreatedAt)
                    .last("limit 1");
        ConsultationMessage lastMessage = consultationMessageMapper.selectOne(queryWrapper);
        return lastMessage != null ? convertToResponseDTO(lastMessage) : null;
    }

    public ConsultationMessageResponseDTO convertToResponseDTO(ConsultationMessage message) {
        ConsultationMessageResponseDTO dto = ConsultationMessageResponseDTO.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .senderType(message.getSenderType())
                .senderTypeDesc(message.getSenderTypeDesc())
                .messageType(message.getMessageType())
                .messageTypeDesc(message.getMessageTypeDesc())
                .content(message.getContent())
                .emotionTag(message.getEmotionTag())
                .aiModel(message.getAiModel())
                .createdAt(message.getCreatedAt())
                .build();
        dto.calculateContentLength();
        return dto;
    }

    public List<ConsultationMessage> getRecentMessages(Long sessionId, int limit) {
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .orderByDesc(ConsultationMessage::getId)
                .last("limit " + limit);
        List<ConsultationMessage> messages = consultationMessageMapper.selectList(queryWrapper);
        //反转消息列表，确保最新消息在最前面
        Collections.reverse(messages);
        return messages;
    }
}
