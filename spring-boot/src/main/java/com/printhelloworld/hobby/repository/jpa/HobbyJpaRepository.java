package com.printhelloworld.hobby.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.printhelloworld.hobby.entity.Hobby;

public interface HobbyJpaRepository extends JpaRepository<Hobby, Long> {
}
