package com.swu.aispringboot.enums;

import lombok.Getter;

@Getter
public enum UserStatus {

    DISABLED(0,"禁用"),
    NORMAL(1, "正常");

    private final Integer code;
    private final String description;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    //根据代码获取枚举
    public static UserStatus fromCode (Integer code){
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                    return status;
            }
        }
        throw new IllegalArgumentException("Invalid UserStatus code: " + code);
    }

}