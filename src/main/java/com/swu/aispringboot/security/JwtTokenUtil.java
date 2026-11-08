package com.swu.aispringboot.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.swu.aispringboot.config.JwtConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class JwtTokenUtil implements ApplicationContextAware {
    private static final String ISSUER = "mental_health_assistant";

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        JwtTokenUtil.applicationContext = applicationContext;
    }

    public static JwtConfig getJwtConfig() {
        return applicationContext.getBean(JwtConfig.class);
    }

    //生成token的方法
    public static String generateToken(Long userId,String username,Integer roleType) {
        try {
            //获取jwt配置
            JwtConfig jwtConfig = getJwtConfig();
            //生成签名的算法
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            //生成过期时间
            Date expirationTime = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());
            //生成token
            String token = JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("roleType", roleType)
                    .withExpiresAt(expirationTime)
                    .withIssuedAt(new Date())
                    .withIssuer(ISSUER)
                    .sign(algorithm);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("生成token失败:" + e);
        }
    }

    //提取token
    public static String extractTokenFromRequest(HttpServletRequest request) {
        if (request == null){
            return null;
        }
        String tokenHeader = request.getHeader("token");
        if (StringUtils.hasText(tokenHeader)){
            return tokenHeader;
        }
        return null;
    }

    //获取当前token
    public static String getCurrentToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null){
            HttpServletRequest request = attributes.getRequest();
            String token = (String) request.getAttribute("token");
            if (token != null){
                return token;
            }
            //备用方案：从请求头中提取token
            String headerToken = extractTokenFromRequest(request);
            if (StringUtils.hasText(headerToken)){
                return headerToken;
            }
        }
        return null;
    }

    //验证token
    public static TokenVerificationResult validateToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        String username = jwt.getClaim("username").asString();
        Integer roleType = null;
        try {
            roleType = jwt.getClaim("roleType").asInt();
        } catch (Exception e) {
            String roleTypeStr = jwt.getClaim("roleType").asString();
            if (StringUtils.hasText(roleTypeStr)) {
                roleType = Integer.valueOf(roleTypeStr);
            }
        }
        if (roleType != null && StringUtils.hasText(username) && userId != null){
            return new TokenVerificationResult(userId, username, roleType, true);
        }
        return null;
    }

    //验证Token的有效性
    public static DecodedJWT verifyToken(String token) {
        if (!StringUtils.hasText(token)){
            throw new JWTVerificationException("token不能为空");
        }
        Algorithm algorithm = Algorithm.HMAC256(getJwtConfig().getSecret());
        JWTVerifier jwtVerifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return jwtVerifier.verify(token);
    }

    //Token验证结果封装类
    @Getter
    public static class TokenVerificationResult {
        private final Long userId;
        private final String username;
        private final Integer roleType;
        private final boolean valid;

        public TokenVerificationResult(Long userId, String username, Integer roleType, boolean valid) {
            this.userId = userId;
            this.username = username;
            this.roleType = roleType;
            this.valid = valid;
        }
    }

    //从token获取userId
    public static Long getUserIdFromToken() {
        String token = getCurrentToken();
        if(token == null || token.isBlank()) {
            throw new RuntimeException("token不存在，请重新登录");
        }
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("userId").asLong();
    }
}
