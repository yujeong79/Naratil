package com.naratil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
		"com.naratil.corporation.repository",
		"com.naratil.user.repository",
		"com.naratil.industry.repository",
		"com.naratil.recommend.repository"
}) // JPA 리포지토리 패키지 지정
@EnableMongoRepositories(basePackages = "com.naratil.bid.repository") // MongoDB 리포지토리 패키지 지정
public class NaratilApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaratilApplication.class, args);
	}
}