package com.swu.aispringboot.controller;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.swu.aispringboot.dto.command.UserLoginCommandDTO;
import com.swu.aispringboot.dto.command.UserRegisterCommandDTO;
import com.swu.aispringboot.dto.response.UserLoginResponseDTO;
import com.swu.aispringboot.common.Result;
import com.swu.aispringboot.service.UserService;
import com.swu.aispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;
    //登录
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO userLoginCommandDTO) {
        UserLoginResponseDTO result = userService.login(userLoginCommandDTO);
        return Result.ok(result);
    }
    //注册
    @PostMapping("/register")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO userRegisterCommandDTO) {
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.register(userRegisterCommandDTO);
        return Result.ok(result);
    }
    //获取当前用户
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser() {
        //从token中获取userId
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT decodedJWT = JwtTokenUtil.verifyToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        //根据userId查询用户信息
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.getUserById(userId);
        return Result.ok(result);
    }
}
