package com.swu.aispringboot.module.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swu.aispringboot.module.chat.enums.MessageType;
import com.swu.aispringboot.module.chat.enums.SenderType;
import com.swu.aispringboot.module.chat.dto.ConsultationMessageResponseDTO;
import com.swu.aispringboot.module.chat.entity.ConsultationMessage;
import com.swu.aispringboot.module.chat.mapper.ConsultationMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
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

    public List<ConsultationMessage> getRecentMessages(Long sessionId, int maxMessages, int maxTokens) {
        //先按条数粗筛选，多取一些作为候选消息
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .orderByDesc(ConsultationMessage::getId)
                .last("limit " + maxMessages);
        List<ConsultationMessage> candidateMessages = consultationMessageMapper.selectList(queryWrapper);

        //从新往旧累加token数，直到超过maxTokens
        List<ConsultationMessage> window = new ArrayList<>();
        int usedTokens = 0;
        for (ConsultationMessage message : candidateMessages) {
            int cost = estimateTokenCost(message.getContent());
            if(!window.isEmpty() && usedTokens + cost > maxTokens) {
                break;
            }
            window.add(message);
            usedTokens += cost;
        }
        Collections.reverse(window);
        log.info("session={}, 候选={}, 窗口={}, 估算token={}, 预算={}",
                sessionId, candidateMessages.size(), window.size(), usedTokens, maxTokens);
        return window;
    }

    private int estimateTokenCost(String content) {
        if(content == null || content.isEmpty()) {
            return 0;
        }
        int ascii = 0;
        int other = 0;
        for (int i = 0; i < content.length(); i++) {
            if(content.charAt(i) < 128) {
                ascii++;
            } else {
                other++;
            }
        }
        return ascii/4 + other + 1;
    }
}
