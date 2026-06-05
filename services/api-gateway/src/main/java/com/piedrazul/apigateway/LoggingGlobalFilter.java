package com.piedrazul.apigateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long start = System.currentTimeMillis();
        logger.info("Incoming request: {} {}", request.getMethod(), request.getURI());

        return chain.filter(exchange)
                .doOnError(ex -> {
                    long duration = System.currentTimeMillis() - start;
                    logger.error("Request {} {} completed with error after {} ms", request.getMethod(), request.getURI(), duration, ex);
                })
                .doFinally(signal -> {
                    long duration = System.currentTimeMillis() - start;
                    Integer status = null;
                    if (exchange.getResponse() != null && exchange.getResponse().getStatusCode() != null) {
                        status = exchange.getResponse().getStatusCode().value();
                    }
                    logger.info("Response {} {} -> {} ({} ms)", request.getMethod(), request.getURI(), status, duration);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
