package com.swu.aispringboot.AiService;


public class StructOutPut {
    public record StreamChatSession(
            String sessionId,
            Long userHash,
            String initialMessage,
            Long startTime,
            Long expireTime,
            Integer messageCount,
            String status
    ) {}
}
