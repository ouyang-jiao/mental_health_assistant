package com.swu.aispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swu.aispringboot.dto.response.ConsultationMessageResponseDTO;
import com.swu.aispringboot.entity.ConsultationMessage;
import com.swu.aispringboot.entity.ConsultationSession;
import com.swu.aispringboot.mapper.ConsultationMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationMessageService {
    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    public ConsultationMessage saveUserMessage(Long sessionId, String content, String emotion_tag) {
        //构建用户消息实体
        ConsultationMessage userMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(1)
                .messageType(1)
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
                .senderType(2)
                .messageType(2)
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
}
