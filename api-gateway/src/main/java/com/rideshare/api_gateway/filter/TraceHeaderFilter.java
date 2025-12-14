// java
package com.rideshare.api_gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceHeaderFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Ensure headers are set right before the response is committed.
        ServerHttpResponse response = exchange.getResponse();
        response.beforeCommit(() -> {
            addTraceHeaders(response.getHeaders());
            return Mono.empty();
        });

        return chain.filter(exchange);
    }

    private void addTraceHeaders(HttpHeaders headers) {
        Span current = Span.current();
        SpanContext ctx = current.getSpanContext();
        if (ctx == null || !ctx.isValid()) {
            return;
        }

        String traceId = ctx.getTraceId();
        String spanId = ctx.getSpanId();
        // W3C traceparent: version(00)-traceId-spanId-flags
        String flags = ctx.isSampled() ? "01" : "00";
        String traceparent = String.format("00-%s-%s-%s", traceId, spanId, flags);

        headers.set("traceparent", traceparent);
        headers.set("traceId", traceId);
        headers.set("spanId", spanId);
        // B3 single header: traceId-spanId-sampled
        headers.set("b3", String.format("%s-%s-%s", traceId, spanId, (ctx.isSampled() ? "1" : "0")));
    }
}
