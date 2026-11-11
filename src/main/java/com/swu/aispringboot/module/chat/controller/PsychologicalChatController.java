package com.swu.aispringboot.module.chat.controller;

import cn.hutool.json.JSONUtil;
import com.swu.aispringboot.exception.BusinessException;
import com.swu.aispringboot.module.chat.service.PsychologicalSupportService;
import com.swu.aispringboot.module.ai.StructOutPut;
import com.swu.aispringboot.common.Result;
import com.swu.aispringboot.common.ResultCode;
import com.swu.aispringboot.module.chat.dto.ConsultationSessionCreateDTO;
import com.swu.aispringboot.module.chat.dto.ConsultationStreamDTO;
import com.swu.aispringboot.security.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChatController {
    @Autowired
    private PsychologicalSupportService psychologicalSupportService;

    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreateDTO consultationSessionCreateDTO) {
        Long userId = JwtTokenUtil.getUserIdFromToken();
        if(userId == null){
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "用户未登录,请重新登录", null);
        }
        StructOutPut.StreamChatSession session = psychologicalSupportService.startSession(userId, consultationSessionCreateDTO);
        return Result.ok(session);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultationStreamDTO consultationStreamDTO) {
        Long userId = JwtTokenUtil.getUserIdFromToken();
        if (userId == null) {
            return Flux.just(errorEvent(ResultCode.UNAUTHORIZED.getCode(), "用户未登录,请重新登录"));
        }
        return psychologicalSupportService.streamPsychologicalChat(userId, consultationStreamDTO.getSessionId(), consultationStreamDTO.getUserMessage())
                .map(fragment -> {
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(JSONUtil.toJsonStr(Result.ok(Map.of("content", fragment, "type", "normal"))))
                            .build();
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()))
                .onErrorResume(BusinessException.class,
                        e -> Flux.just(errorEvent(e.getCode(), e.getMessage())))
                .onErrorResume(e -> {
                    log.error("流式对话异常，sessionId={}", consultationStreamDTO.getSessionId(), e);
                    return Flux.just(errorEvent(ResultCode.SYSTEM_ERROR.getCode(), "服务器异常，请稍后重试"));
                });
    }

    private ServerSentEvent<String> errorEvent(String code, String message) {
        return ServerSentEvent.<String>builder()
                .event("error")
                .data(JSONUtil.toJsonStr(Result.error(code, message, null)))
                .build();
    }
}
