package com.swu.aispringboot.module.chat.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    TEXT(1, "文本消息");

    private final Integer code;
    private final String desc;

    MessageType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MessageType fromCode(Integer code) {
        for (MessageType messageType : values()) {
            if (messageType.code.equals(code)) {
                return messageType;
            }
        }
        return null;
    }
}
