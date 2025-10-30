package com.rideshare.api_gateway.config;

import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayTracingConfig {

    @Bean
    public GlobalFilter tracingGlobalFilter(Tracer tracer) {
        return (exchange, chain) -> {
            // continue filter chain first
            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> addTraceInfoToLogs(exchange, tracer)));
        };
    }

    private void addTraceInfoToLogs(ServerWebExchange exchange, Tracer tracer) {
        if (tracer == null || tracer.currentSpan() == null) {
            System.out.println("❌ No active span found for tracing");
            return;
        }

        String traceId = tracer.currentSpan().context().traceId();
        String spanId = tracer.currentSpan().context().spanId();
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);

        System.out.println("✅ Gateway Trace -> traceId=" + traceId + ", spanId=" + spanId);
    }
}
