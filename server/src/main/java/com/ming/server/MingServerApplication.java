package com.ming.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MingServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MingServerApplication.class, args);
    }
}