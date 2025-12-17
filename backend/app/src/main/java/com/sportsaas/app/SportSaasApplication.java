package com.sportsaas.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.sportsaas")
@EntityScan(basePackages = "com.sportsaas")
@EnableJpaRepositories(basePackages = "com.sportsaas")
@EnableJpaAuditing
public class SportSaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportSaasApplication.class, args);
    }
}
