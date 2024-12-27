package com.house.agents.controller;

import com.house.agents.result.R;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Api(tags = "健康检查模块")
@CrossOrigin
@Slf4j
public class HealthController {

    @GetMapping("/monitor/alive")
    public R health() {
        return R.ok();
    }
}
