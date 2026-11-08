package com.swu.aispringboot.module.chat.service;

import com.swu.aispringboot.module.ai.PromptManage;
import com.swu.aispringboot.module.ai.StructOutPut;
import com.swu.aispringboot.module.chat.dto.ConsultationSessionCreateDTO;
import com.swu.aispringboot.module.chat.dto.ConsultationMessageResponseDTO;
import com.swu.aispringboot.module.chat.entity.ConsultationMessage;
import com.swu.aispringboot.module.chat.entity.ConsultationSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
public class PsychologicalSupportService {
    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO consultationSessionCreateDTO) {
        //创建数据库会话记录
        ConsultationSession consultationSession = consultationSessionService.createSession(userId, consultationSessionCreateDTO);
        //将初始用户消息保存到Message表
        ConsultationMessage userMessage = consultationMessageService.saveUserMessage(consultationSession.getId(), consultationSessionCreateDTO.getInitialMessage(), null);
        //创建会话信息
        String sessionId = "session_" + consultationSession.getId();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                consultationSessionCreateDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L,
                1,
                "ACTIVE");
    }

    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage) {
        //创建响应流
        return Flux.create(sink -> {
            Long dbSessionId = extractSessionId(sessionId);
            if (dbSessionId == null) {
                sink.error(new RuntimeException("会话ID格式错误"));
                return;
            }
            //是否为初始消息
            boolean isInitialMessage = false;
            //检查会话是否存在
            Integer messageCount = consultationMessageService.getMessageCountBySessionId(dbSessionId);
            if (messageCount == 1) {
                ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(dbSessionId);
                if (lastMessage != null && lastMessage.getSenderType() == 1 && userMessage.equals(lastMessage.getContent())) {
                    isInitialMessage = true;
                }
            }
            if (!isInitialMessage) {
                //保存用户信息到数据库
                consultationMessageService.saveUserMessage(dbSessionId, userMessage, null);
            }
            //进行流式对话
            //生成对话记忆管理
            String conversationId = "conversation_" + sessionId;
            //构建系统提示词
            List<Message> userMessages = new ArrayList<>();
            userMessages.add(new UserMessage(userMessage));
            chatMemory.add(conversationId, userMessages);
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
            ));
            //用于存储ai完整响应
            StringBuilder fullResponse = new StringBuilder();
            //使用ChatClient发送消息到模型
            chatClient.prompt(prompt)
                    .messages(userMessages)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content()
                    .doOnNext(Fragment -> {
                        fullResponse.append(Fragment);
                        sink.next(Fragment);
                    })
                    .doOnComplete(() -> {
                        //将AI返回的内容保存到数据库
                        consultationMessageService.saveAiMessage(dbSessionId, fullResponse.toString(), "open-ai");
                        //添加ai回复到对话记忆
                        List<Message> aiMessages = new ArrayList<>();
                        aiMessages.add(new AssistantMessage(fullResponse.toString()));
                        chatMemory.add(conversationId, aiMessages);
                        sink.complete();
                    })
                    .doOnError(error -> {
                            sink.error(error);
                    })
                    .subscribe();//订阅并启动流

        });

    }

    //获取参数中的sessionId
    public Long extractSessionId(String sessionId) {
        if (sessionId != null && sessionId.startsWith("session_")) {
            return Long.parseLong(sessionId.substring("session_".length()));
        }
        return null;
    }
}
