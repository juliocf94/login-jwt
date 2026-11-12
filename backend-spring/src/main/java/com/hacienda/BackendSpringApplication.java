package com.hacienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada. @SpringBootApplication escanea com.hacienda.* y arranca Tomcat. */
@SpringBootApplication
public class BackendSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendSpringApplication.class, args);
    }
}
