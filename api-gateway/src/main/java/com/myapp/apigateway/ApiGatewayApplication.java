

package com.myapp.apigateway;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
@OpenAPIDefinition
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return  builder.routes()
                .route("authentification", r->r.path("/authentification/**")
                        .uri("http://localhost:8084/"))
                .route("licenses", r->r.path("/licenses/**")
                        .uri("http://localhost:8085/"))
                .route("tenant", r->r.path("/tenant/**")
                        .uri("http://localhost:8086/"))
                .route("sip-users", r->r.path("/sip-users/**")
                        .uri("http://localhost:8086/"))
                .build();
    }

}