package com.sync_BE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.sync_BE.repository")
public class SumNewsBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SumNewsBeApplication.class, args);
	}

}
