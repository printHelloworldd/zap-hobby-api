package com.printhelloworld.hobby.service;

import org.springframework.stereotype.Service;

import com.printhelloworld.hobby.entity.Category;
import com.printhelloworld.hobby.repository.jpa.CategoryJpaRepository;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryJpaRepository categoryRepository;

    public CategoryService(CategoryJpaRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}
