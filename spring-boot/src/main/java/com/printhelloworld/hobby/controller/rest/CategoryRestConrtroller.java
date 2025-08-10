package com.printhelloworld.hobby.controller.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.printhelloworld.hobby.entity.Category;
import com.printhelloworld.hobby.service.CategoryService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.List;

@RestController("restCategoryController")
@RequestMapping("api/v1/categories")
public class CategoryRestConrtroller {
    private final CategoryService categoryService;
    private final Bucket bucket;

    public CategoryRestConrtroller(CategoryService categoryService) {
        this.categoryService = categoryService;
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @GetMapping
    public ResponseEntity<List<Category>> getCategories() {
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(categoryService.getCategories());
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}
