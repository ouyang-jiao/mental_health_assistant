package com.swu.aispringboot.module.chat.enums;

import lombok.Getter;

@Getter
public enum SenderType {
    USER(1, "用户"),
    AI(2, "AI");

    private final Integer code;
    private final String desc;

    SenderType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SenderType fromCode(Integer code) {
        for (SenderType senderType : values()) {
            if (senderType.code.equals(code)) {
                return senderType;
            }
        }
        return null;
    }
}
