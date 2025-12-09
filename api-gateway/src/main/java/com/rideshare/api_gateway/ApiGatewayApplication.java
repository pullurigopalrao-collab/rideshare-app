package com.rideshare.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import reactor.core.publisher.Hooks;

@EnableDiscoveryClient
@SpringBootApplication
public class ApiGatewayApplication {

    // ✅ Move the hook call here to ensure it runs first
    static {
        Hooks.enableAutomaticContextPropagation();
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }


}
