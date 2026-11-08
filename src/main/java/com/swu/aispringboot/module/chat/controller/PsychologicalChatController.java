package com.swu.aispringboot.module.chat.controller;

import cn.hutool.json.JSONUtil;
import com.swu.aispringboot.module.chat.service.PsychologicalSupportService;
import com.swu.aispringboot.module.ai.StructOutPut;
import com.swu.aispringboot.common.Result;
import com.swu.aispringboot.common.ResultCode;
import com.swu.aispringboot.module.chat.dto.ConsultationSessionCreateDTO;
import com.swu.aispringboot.module.chat.dto.ConsultationStreamDTO;
import com.swu.aispringboot.security.JwtTokenUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChatController {
    @Autowired
    private PsychologicalSupportService psychologicalSupportService;

    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreateDTO consultationSessionCreateDTO) {
        Long userId = JwtTokenUtil.getUserIdFromToken();
        StructOutPut.StreamChatSession session = psychologicalSupportService.startSession(userId, consultationSessionCreateDTO);
        return Result.ok(session);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultationStreamDTO consultationStreamDTO){
        Long userId = JwtTokenUtil.getUserIdFromToken();
        if (userId == null) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(JSONUtil.toJsonStr(Result.error(ResultCode.UNAUTHORIZED.getCode(),ResultCode.UNAUTHORIZED.getMsg(), "用户未登录")))
                    .build());
        }
        return psychologicalSupportService.streamPsychologicalChat(consultationStreamDTO.getSessionId(), consultationStreamDTO.getUserMessage())
                .map(Fragment -> {
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(JSONUtil.toJsonStr(Result.ok(Map.of("content", Fragment, "type", "normal"))))
                            .build();
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()))
                .delayElements(Duration.ofMillis(50));// 模拟延迟，避免快速响应导致浏览器卡顿

    }
 }
