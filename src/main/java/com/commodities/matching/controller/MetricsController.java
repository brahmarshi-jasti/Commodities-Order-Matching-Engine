package com.commodities.matching.controller;

import com.commodities.matching.metrics.MetricsCollector;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    private final MetricsCollector metricsCollector;

    public MetricsController(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @GetMapping
    public MetricsCollector.EngineMetrics getMetrics() {
        return metricsCollector.getMetrics();
    }
}
