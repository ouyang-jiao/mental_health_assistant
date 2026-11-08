package com.swu.aispringboot.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.swu.aispringboot.common.ResultCode;
import com.swu.aispringboot.config.SecurityConfig;
import com.swu.aispringboot.exception.BusinessException;
import com.swu.aispringboot.module.auth.dto.UserLoginResponseDTO;
import com.swu.aispringboot.module.auth.enums.UserStatus;
import com.swu.aispringboot.module.auth.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    private UserService userService;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityConfig.isPublicPath(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        // 1.提取token
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }
        JwtTokenUtil.TokenVerificationResult validationResult;
        // 2.验证token
        try {
            validationResult = JwtTokenUtil.validateToken(token);
        }catch (TokenExpiredException e){
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.TOKEN_EXPIRED);
            return;
        }catch (JWTVerificationException e){
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
            return;
        }
        // 3.claim不完整或无效
        if (validationResult == null || !validationResult.isValid()) {
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
            return;
        }
        // 4.查询用户信息验证用户状态
        UserLoginResponseDTO.UserDetailResponseDTO user;
        try {
            user = userService.getUserById(validationResult.getUserId());
        }catch (BusinessException e){
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
            return;
        }

        if (user == null || !UserStatus.NORMAL.getCode().equals(user.getStatus())) {
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
            return;
        }
        // 5.放入Spring Security上下文
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + validationResult.getRoleType())
        );
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                validationResult.getUsername(),//用户名作为主体
                null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        request.setAttribute("token", token);
        // 6.放行
        chain.doFilter(request, response);
    }

    //清理Spring Security上下文
    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}

