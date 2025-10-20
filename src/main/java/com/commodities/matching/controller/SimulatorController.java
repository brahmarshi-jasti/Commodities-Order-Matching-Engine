package com.commodities.matching.controller;

import com.commodities.matching.service.OrderSimulator;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {
    private final OrderSimulator orderSimulator;

    public SimulatorController(OrderSimulator orderSimulator) {
        this.orderSimulator = orderSimulator;
    }

    @PostMapping("/start")
    public Map<String, Object> start() {
        orderSimulator.setEnabled(true);
        return Map.of("status", "started");
    }

    @PostMapping("/stop")
    public Map<String, Object> stop() {
        orderSimulator.setEnabled(false);
        return Map.of("status", "stopped");
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("enabled", orderSimulator.isEnabled());
    }
}
