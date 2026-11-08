package com.swu.aispringboot.service.convert;

import com.swu.aispringboot.dto.command.UserRegisterCommandDTO;
import com.swu.aispringboot.dto.response.UserLoginResponseDTO;
import com.swu.aispringboot.entity.User;
import com.swu.aispringboot.enums.UserStatus;

import java.time.LocalDateTime;

public class UserConvert {
    //构建响应DTO
    public static UserLoginResponseDTO.UserDetailResponseDTO entityToDetailResponseDTO(User user) {
        return UserLoginResponseDTO.UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar (user.getAvatar ())
                .phone(user.getPhone())
                .gender (user.getGender ())
                .genderDisplayName(getGenderDisplayName(user.getGender()))
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .status (user.getStatus ())
                .statusDisplayName(user.getStatusDisplayName())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .updatedAt (user.getUpdatedAt())
                .build();
    }

    public static UserLoginResponseDTO entityToLoginResponseDTO(String token, UserLoginResponseDTO.UserDetailResponseDTO userInfo) {
        return UserLoginResponseDTO.builder()
                .token(token)
                .roleType(userInfo.getUserType().toString())
                .userInfo(userInfo)
                .build();
    }

    public static User registerCommandToEntity(UserRegisterCommandDTO userRegisterCommandDTO, String encodedPassword) {
        return User.builder()
                .username(userRegisterCommandDTO.getUsername())
                .email(userRegisterCommandDTO.getEmail())
                .phone(userRegisterCommandDTO.getPhone())
                .password(encodedPassword)
                .nickname(userRegisterCommandDTO.getNickname())
                .gender(userRegisterCommandDTO.getGender())
                .birthday(userRegisterCommandDTO.getBirthday())
                .userType(userRegisterCommandDTO.getUserType())
                .status(UserStatus.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    //根据性别获取显示名称
    private static String getGenderDisplayName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        return gender == 1 ? "男" : "女";
    }
}
