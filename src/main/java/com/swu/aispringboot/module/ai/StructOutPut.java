package com.swu.aispringboot.module.ai;


public class StructOutPut {
    public record StreamChatSession(
            Long sessionId,
            Long userHash,
            String initialMessage,
            Long startTime,
            Long expireTime,
            Integer messageCount,
            String status
    ) {}
}
