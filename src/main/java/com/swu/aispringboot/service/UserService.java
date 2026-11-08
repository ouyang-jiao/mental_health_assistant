package com.swu.aispringboot.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swu.aispringboot.dto.command.UserRegisterCommandDTO;
import com.swu.aispringboot.enums.UserType;
import com.swu.aispringboot.service.convert.UserConvert;
import com.swu.aispringboot.dto.command.UserLoginCommandDTO;
import com.swu.aispringboot.dto.response.UserLoginResponseDTO;
import com.swu.aispringboot.entity.User;
import com.swu.aispringboot.exception.BusinessException;
import com.swu.aispringboot.mapper.UserMapper;
import com.swu.aispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    public UserLoginResponseDTO login(UserLoginCommandDTO userLoginCommandDTO) {
        //构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userLoginCommandDTO.getUsername())
                .or()
                .eq(User::getEmail, userLoginCommandDTO.getUsername());
        //调用MP API查询用户
        User user = userMapper.selectOne(queryWrapper);
        //System.out.println(user);
        //判断用户是否存在
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        //判断密码是否正确
        String inputPassword = userLoginCommandDTO.getPassword().trim();
        if(!bCryptPasswordEncoder.matches(inputPassword, user.getPassword())){
            throw new BusinessException("密码错误");
        }
        //检查用户状态
        if (!user.isActive()) {
            throw new BusinessException("用户已禁用, 请联系管理员");
        }
        //生成JWT token
        String token = JwtTokenUtil.generateToken(user.getId(),user.getUsername(),user.getUserType());
        //System.out.println(token);
        //构建响应DTO
        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponseDTO(user);
        //返回成功结果
        return UserConvert.entityToLoginResponseDTO(token, userInfo);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO userRegisterCommandDTO) {
        //System.out.println(JSONUtil.parseObj(userRegisterCommandDTO));
        //验证密码是否一致
        if(!userRegisterCommandDTO.getPassword().equals(userRegisterCommandDTO.getConfirmPassword())){
            throw new BusinessException("两次输入密码不一致");
        }
        //检查用户名是否存在
        LambdaQueryWrapper<User> UserNameQueryWrapper = new LambdaQueryWrapper<>();
        UserNameQueryWrapper.eq(User::getUsername, userRegisterCommandDTO.getUsername());
        if (userMapper.selectCount(UserNameQueryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        //检查邮箱是否存在
        LambdaQueryWrapper<User> EmailQueryWrapper = new LambdaQueryWrapper<>();
        EmailQueryWrapper.eq(User::getEmail, userRegisterCommandDTO.getEmail());
        if (userMapper.selectCount(EmailQueryWrapper) > 0) {
            throw new BusinessException("邮箱已注册");
        }
        //用户类型
        if (!UserType.isValidCode(userRegisterCommandDTO.getUserType())) {
            throw new BusinessException("用户类型无效");
        }
        //创建用户实体对象
        User user = UserConvert.registerCommandToEntity(userRegisterCommandDTO, bCryptPasswordEncoder.encode(userRegisterCommandDTO.getPassword().trim()));
        //调用MP API插入用户
        userMapper.insert(user);
        return UserConvert.entityToDetailResponseDTO(user);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserConvert.entityToDetailResponseDTO(user);
    }
}
