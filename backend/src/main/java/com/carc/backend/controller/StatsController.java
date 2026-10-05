package com.carc.backend.controller;

import com.carc.backend.stats.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 流量统计接口：GET /api/stats
 */
@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/api/stats")
    public Map<String, Object> stats() {
        return statsService.snapshot();
    }
}