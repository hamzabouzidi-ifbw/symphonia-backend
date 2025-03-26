

package com.myapp.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
/*    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return  builder.routes()
                .route("MS-Formation", r->r.path("/formation/**")
                        .uri("http://localhost:8090/"))
                .route("MS-Partenariat", r->r.path("/partenariat/**")
                        .uri("http://localhost:8091/"))

                .route("MS-Projet", r->r.path("/projet/**")
                        .uri("http://localhost:8093/"))

                .route("MS-Reclamation", r->r.path("/reclamation/**")
                        .uri("http://localhost:8092/"))
                .route("MS-Recolte", r->r.path("/recolte/**")
                        .uri("http://localhost:8094/"))
                .route("MS-Ressources", r->r.path("/ressources/**")
                        .uri("http://localhost:8095/"))
                // Nouvelle route pour le microservice Node.js MS-User
                *//* .route("MS-User", r -> r.path("/user/**")
                         .uri("http://localhost:3000/"))*//*
                .build();
    }*/

}