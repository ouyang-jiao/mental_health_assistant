package com.swu.aispringboot.module.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.swu.aispringboot.module.chat.enums.MessageType;
import com.swu.aispringboot.module.chat.enums.SenderType;
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
        SenderType st = SenderType.fromCode(senderType);
        return st == null ? "未知" : st.getDesc();
    }

    public String  getMessageTypeDesc() {
        if (messageType == null) {
            return "未知";
        }
        MessageType mt = MessageType.fromCode(messageType);
        return mt == null ? "未知" : mt.getDesc();
    }
}
