package com.printhelloworld.hobby.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.printhelloworld.hobby.repository.jpa")
public class JpaConfig {

}
