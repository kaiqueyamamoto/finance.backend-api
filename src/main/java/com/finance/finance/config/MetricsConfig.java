package com.finance.finance.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("finance.user.registrations")
                .description("Total number of user registrations")
                .register(meterRegistry);
    }

    @Bean
    public Counter loginAttemptsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("finance.auth.login.attempts")
                .description("Total number of login attempts")
                .tag("result", "attempt")
                .register(meterRegistry);
    }

    @Bean
    public Counter successfulLoginsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("finance.auth.login.success")
                .description("Total number of successful logins")
                .register(meterRegistry);
    }

    @Bean
    public Counter failedLoginsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("finance.auth.login.failed")
                .description("Total number of failed logins")
                .register(meterRegistry);
    }

    @Bean
    public Timer apiRequestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("finance.api.request.duration")
                .description("API request duration")
                .register(meterRegistry);
    }

    @Bean
    public Counter databaseOperationsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("finance.database.operations")
                .description("Total number of database operations")
                .tag("operation", "query")
                .register(meterRegistry);
    }
}
