package com.printhelloworld.hobby.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.printhelloworld.hobby.entity.Hobby;
import com.printhelloworld.hobby.repository.jpa.HobbyJpaRepository;

import java.util.List;

@Service
public class HobbyService {
    private final HobbyJpaRepository hobbyRepository;
    private static final Logger logger = LoggerFactory.getLogger(HobbyService.class);

    public HobbyService(HobbyJpaRepository hobbyRepository) {
        this.hobbyRepository = hobbyRepository;
    }

    @Cacheable(value = "HOBBY_CACHE", key = "'all'")
    public Hobby[] getHobbies() {
        logger.info("Getting data from postgres");
        return hobbyRepository.findAll().toArray(new Hobby[0]);
    }

    public void insertHobbies(List<Hobby> hobbies) {
        hobbyRepository.saveAll(hobbies);
    }
}
