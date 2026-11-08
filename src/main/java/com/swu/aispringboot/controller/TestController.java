package com.swu.aispringboot.controller;

import com.swu.aispringboot.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("/test")
    public Result<String> test() {
        return Result.ok("This is a test!");
    }
}
