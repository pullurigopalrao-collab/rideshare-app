package com.rideshare.api_gateway;

import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

@EnableDiscoveryClient
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public WebFilter mdcTraceFilter(Tracer tracer) {
        return (exchange, chain) -> chain.filter(exchange)
                .contextWrite(ctx -> {
                    var span = tracer.currentSpan();
                    if (span != null) {
                        MDC.put("traceId", span.context().traceId());
                        MDC.put("spanId", span.context().spanId());
                    }
                    return ctx;
                })
                .doFinally(signalType -> {
                    MDC.remove("traceId");
                    MDC.remove("spanId");
                });
    }
}
