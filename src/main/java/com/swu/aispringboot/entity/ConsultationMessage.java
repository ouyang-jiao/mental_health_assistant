package com.swu.aispringboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("consultation_message")
@Builder
public class ConsultationMessage   {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "会话ID不能为空")
    @TableField("session_id")
    private Long sessionId;

    @NotNull(message = "发送者类型不能为空")
    @TableField("sender_type")
    private Integer senderType;

    @NotNull(message = "消息类型不能为空")
    @TableField("message_type")
    private Integer messageType;

    @NotNull(message = "消息内容不能为空")
    private String content;

    @Size(max = 50, message = "情感标签长度不能超过50个字符")
    @TableField("emotion_tag")
    private String emotionTag;

    @Size(max = 50, message = "AI模型长度不能超过50个字符")
    @TableField("ai_model")
    private String aiModel;

    @TableField("created_at")
    private LocalDateTime createdAt;

    public String  getSenderTypeDesc() {
        if (senderType == null) {
            return "未知";
        }
        return switch (senderType) {
            case 0 -> "用户";
            case 1 -> "AI";
            default -> "未知";
        };
    }

    public String  getMessageTypeDesc() {
        if (messageType == null) {
            return "未知";
        }
        return switch (messageType) {
            case 0 -> "文本";
            default -> "未知";
        };
    }
}
