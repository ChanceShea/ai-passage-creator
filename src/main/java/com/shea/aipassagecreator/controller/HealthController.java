package com.shea.aipassagecreator.controller;

import com.shea.aipassagecreator.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 * @author : Shea.
 * @since : 2026/5/17 15:17
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public Result<String> healthCheck() {
        return Result.success("ok");
    }
}
