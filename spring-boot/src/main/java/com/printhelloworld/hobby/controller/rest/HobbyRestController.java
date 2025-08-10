package com.printhelloworld.hobby.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.printhelloworld.hobby.entity.Hobby;
import com.printhelloworld.hobby.service.HobbyService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;

@RestController("restHobbyController")
@RequestMapping("api/v1/hobbies")
public class HobbyRestController {
    private final HobbyService hobbyService;
    private final Bucket bucket;
    private static final Logger logger = LoggerFactory.getLogger(HobbyRestController.class);

    public HobbyRestController(HobbyService hobbyService) {
        this.hobbyService = hobbyService;
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @GetMapping
    public ResponseEntity<Hobby[]> getHobbies() {
        if (bucket.tryConsume(1)) {
            long startTime = System.currentTimeMillis();
            long duration = System.currentTimeMillis() - startTime;

            final Hobby[] hobbies = hobbyService.getHobbies();

            logger.info("REST: Returned {} hobbies for {} мс", hobbies.length, duration);

            return ResponseEntity.ok(hobbies);
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}
