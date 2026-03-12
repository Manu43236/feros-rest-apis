package com.feros.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FerosApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FerosApiApplication.class, args);
	}

}
