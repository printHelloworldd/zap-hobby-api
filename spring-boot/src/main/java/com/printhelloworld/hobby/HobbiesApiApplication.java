package com.printhelloworld.hobby;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class HobbiesApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(HobbiesApiApplication.class, args);
	}
}
