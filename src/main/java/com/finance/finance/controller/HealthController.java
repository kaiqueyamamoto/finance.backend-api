package com.finance.finance.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class HealthController {

    private final MeterRegistry meterRegistry;
    private final Counter healthCheckCounter;

    public HealthController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.healthCheckCounter = Counter.builder("finance.health.checks")
                .description("Total number of health check requests")
                .register(meterRegistry);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        // Incrementar contador de health checks
        healthCheckCounter.increment();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "finance-backend-api");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }

    @GetMapping("/")
    public Map<String, String> welcome() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to Finance Backend API");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("metrics", "Available at /actuator/prometheus");
        return response;
    }
}
