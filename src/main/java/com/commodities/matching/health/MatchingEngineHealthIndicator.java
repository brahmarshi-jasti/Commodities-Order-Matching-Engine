package com.commodities.matching.health;

import com.commodities.matching.engine.MatchingEngine;
import com.commodities.matching.metrics.MetricsCollector;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MatchingEngineHealthIndicator implements HealthIndicator {
    
    private final MatchingEngine matchingEngine;
    private final MetricsCollector metricsCollector;
    
    public MatchingEngineHealthIndicator(MatchingEngine matchingEngine, MetricsCollector metricsCollector) {
        this.matchingEngine = matchingEngine;
        this.metricsCollector = metricsCollector;
    }
    
    @Override
    public Health health() {
        try {
            MetricsCollector.EngineMetrics metrics = metricsCollector.getMetrics();
            
            // Check if engine is operational
            if (matchingEngine.getAllOrderBooks() == null || matchingEngine.getAllOrderBooks().isEmpty()) {
                return Health.down()
                    .withDetail("reason", "Order books not initialized")
                    .build();
            }
            
            // Check latency performance
            double avgLatency = metrics.avgLatencyMicros;
            if (avgLatency > 1000) { // More than 1ms average
                return Health.up()
                    .withDetail("status", "degraded")
                    .withDetail("avgLatencyMicros", avgLatency)
                    .withDetail("warning", "Latency exceeds target")
                    .withDetail("totalOrders", metrics.totalOrders)
                    .withDetail("totalTrades", metrics.totalTrades)
                    .build();
            }
            
            return Health.up()
                .withDetail("status", "healthy")
                .withDetail("avgLatencyMicros", avgLatency)
                .withDetail("totalOrders", metrics.totalOrders)
                .withDetail("totalTrades", metrics.totalTrades)
                .withDetail("orderBooks", matchingEngine.getAllOrderBooks().size())
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
