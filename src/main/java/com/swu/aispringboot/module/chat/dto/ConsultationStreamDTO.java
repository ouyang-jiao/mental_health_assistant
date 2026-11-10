package com.swu.aispringboot.module.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultationStreamDTO {
    @NotNull(message = "sessionId不能为空")
    private Long sessionId;

    @NotBlank(message = "初始消息不能为空")
    @Size(max = 2000, message = "初始消息不能超过2000个字符")
    private String userMessage;
}
