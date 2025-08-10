package com.printhelloworld.hobby.controller.graphql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.printhelloworld.hobby.entity.Hobby;
import com.printhelloworld.hobby.service.HobbyService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;

@Controller("graphqlHobbyController")
public class HobbyGraphqlController {
    private final HobbyService hobbyService;
    private final Bucket bucket;
    private static final Logger logger = LoggerFactory.getLogger(HobbyGraphqlController.class);

    public HobbyGraphqlController(HobbyService hobbyService) {
        this.hobbyService = hobbyService;
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @QueryMapping(name = "hobbies")
    public Hobby[] getHobbies() {
        if (bucket.tryConsume(1)) {
            long startTime = System.currentTimeMillis();
            long duration = System.currentTimeMillis() - startTime;

            final Hobby[] hobbies = hobbyService.getHobbies();

            logger.info("GraphQL: Returned {} hobbies for {} мс", hobbies.length, duration);

            return hobbies;
        } else {
            throw new RuntimeException("Too many requests");
        }
    }
}
