package com.swu.aispringboot.module.chat.service;

import com.swu.aispringboot.exception.BusinessException;
import com.swu.aispringboot.module.ai.PromptManage;
import com.swu.aispringboot.module.ai.StructOutPut;
import com.swu.aispringboot.module.chat.dto.ConsultationSessionCreateDTO;
import com.swu.aispringboot.module.chat.dto.ConsultationMessageResponseDTO;
import com.swu.aispringboot.module.chat.entity.ConsultationMessage;
import com.swu.aispringboot.module.chat.entity.ConsultationSession;
import com.swu.aispringboot.module.chat.enums.SenderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PsychologicalSupportService {
    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    @Value("${chat.history.max-messages:50}")
    private int maxHistoryMessages;

    @Value("${chat.history.max-tokens:3000}")
    private int maxTokens;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO consultationSessionCreateDTO) {
        //创建数据库会话记录
        ConsultationSession consultationSession = consultationSessionService.createSession(userId, consultationSessionCreateDTO);
        //将初始用户消息保存到Message表
        consultationMessageService.saveUserMessage(consultationSession.getId(),
                consultationSessionCreateDTO.getInitialMessage(), null);
        
        Long sessionId = consultationSession.getId();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                consultationSessionCreateDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L,
                1,
                "ACTIVE");
    }

    public Flux<String> streamPsychologicalChat(Long userId, Long sessionId, String userMessage) {
        //创建响应流
        return Flux.create(sink -> {
            //先校验归属关系
            try {
                consultationSessionService.assertSessionOwner(sessionId, userId);
            } catch (BusinessException e) {
                sink.error(e);
                return;
            }
            //是否为初始消息
            boolean isInitialMessage = false;
            //检查会话是否存在
            Integer messageCount = consultationMessageService.getMessageCountBySessionId(sessionId);
            if (messageCount == 1) {
                ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(sessionId);
                if (lastMessage != null && lastMessage.getSenderType() == 1 && userMessage.equals(lastMessage.getContent())) {
                    isInitialMessage = true;
                }
            }
            if (!isInitialMessage) {
                //保存用户信息到数据库
                consultationMessageService.saveUserMessage(sessionId, userMessage, null);
            }
            //进行流式对话
            //用于存储ai完整响应
            StringBuilder fullResponse = new StringBuilder();

            List<Message> historyMessages = new ArrayList<>();
            for(ConsultationMessage message : consultationMessageService.getRecentMessages(sessionId, maxHistoryMessages, maxTokens)){
                historyMessages.add(SenderType.USER.getCode().equals(message.getSenderType())
                        ? new UserMessage(message.getContent())
                        : new AssistantMessage(message.getContent()));
            }

            //使用ChatClient发送消息到模型
            chatClient.prompt()
                    .system(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
                    .messages(historyMessages)
                    .stream()
                    .content()
                    .doOnNext(fragment -> {
                        fullResponse.append(fragment);
                        sink.next(fragment);
                    })
                    .doOnError(sink::error)
                    .doFinally(signalType -> {
                        if(!fullResponse.isEmpty()){
                            try{
                                consultationMessageService.saveAiMessage(sessionId, fullResponse.toString(), "open-ai");
                            }catch (Exception e){
                                log.error("保存AI回复到数据库失败, sessionId: {}", sessionId, e);
                            }
                        }
                        if(signalType == SignalType.ON_COMPLETE){
                            sink.complete();
                        }
                    })
                    .subscribe();//订阅并启动流

        });

    }
}
