package com.swu.aispringboot.security;

import org.springframework.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.swu.aispringboot.common.Result;
import com.swu.aispringboot.common.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ResponseUtil {
    //过滤器中的异常响应
    public static void writeError(HttpServletResponse response, ResultCode resultCode) {
        //根据resultCode返回不同响应
        int status = switch (resultCode) {
            case UNAUTHORIZED, ACCESS_UNAUTHORIZED, TOKEN_INVALID, TOKEN_EXPIRED, TOKEN_BLOCKED ->
                    HttpStatus.UNAUTHORIZED.value();
            case TOKEN_ACCESS_FORBIDDEN -> HttpStatus.FORBIDDEN.value();
            case AUTHORIZED_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR.value();
            default -> HttpStatus.BAD_REQUEST.value();
        };
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (PrintWriter writer = response.getWriter()) {
            String jsonResponse = JSONUtil.toJsonStr(Result.error(resultCode.getCode(), resultCode.getMsg(), null));
            writer.print(jsonResponse);
            writer.flush();//刷新缓冲区，确保响应立即发送
        } catch (IOException e) {
            System.out.println("写入响应失败：" + e.getMessage());
        }
    }
}
