package com.printhelloworld.hobby.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.printhelloworld.hobby.entity.Category;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
}
