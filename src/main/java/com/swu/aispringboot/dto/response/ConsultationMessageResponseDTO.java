package com.swu.aispringboot.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultationMessageResponseDTO {
    private Long id;
    private Long sessionId;
    private Integer senderType;
    private String senderTypeDesc;
    private Integer messageType;
    private String messageTypeDesc;
    private String content;
    private String emotionTag;
    private String aiModel;
    private LocalDateTime createdAt;
    private Integer contentLength;

    public void calculateContentLength() {
        this.contentLength = content != null ? content.length() : 0;
    }
}
