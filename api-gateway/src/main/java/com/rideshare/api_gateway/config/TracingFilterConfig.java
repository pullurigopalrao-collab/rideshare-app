/*
package com.rideshare.api_gateway.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.http.HttpServerHandler;
import io.micrometer.tracing.http.HttpServerRequest;
import io.micrometer.tracing.http.HttpServerResponse;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class TracingFilterConfig {

    private final HttpServerHandler httpTracingHandler;
    private final ObservationRegistry observationRegistry;

    // Inject the necessary tracing components
    public TracingFilterConfig(HttpServerHandler httpTracingHandler, ObservationRegistry observationRegistry) {
        this.httpTracingHandler = httpTracingHandler;
        this.observationRegistry = observationRegistry;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this runs first
    public GlobalFilter tracingGlobalFilter() {
        return (exchange, chain) -> {
            // Start the trace using the handler (which injects headers into outgoing request/MDC)
            HttpServerRequest request = new GatewayHttpServerRequest(exchange.getRequest());

            // handleStart creates the observation/span context
            httpTracingHandler.handleStart(request, observationRegistry);

            return chain.filter(exchange)
                    .doFinally(signalType -> {
                        // This block runs when the response is processed
                        HttpServerResponse response = new GatewayHttpServerResponse(exchange.getResponse());
                        // handleEnd finds the observation from the context and ends it properly
                        httpTracingHandler.handleEnd(response, observationRegistry.getCurrentObservation());
                    });
        };
    }

    // You will need to implement a simple adapter class to map ServerHttpRequest/ServerHttpResponse
    // to Micrometer's generic HttpServerRequest/HttpServerResponse interfaces if you haven't already.
    // (This is usually provided by Spring Boot autoconfig, but adding it explicitly might solve the 'Cannot resolve' issue)
}
*/
