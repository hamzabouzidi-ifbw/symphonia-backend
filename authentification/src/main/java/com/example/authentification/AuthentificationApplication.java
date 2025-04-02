package com.example.authentification;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@OpenAPIDefinition
@SpringBootApplication
public class AuthentificationApplication {

    public static void main(String[] args) {

        SpringApplication.run(AuthentificationApplication.class, args);
    }

}
