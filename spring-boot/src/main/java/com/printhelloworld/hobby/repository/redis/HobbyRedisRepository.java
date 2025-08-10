package com.printhelloworld.hobby.repository.redis;

import org.springframework.data.repository.CrudRepository;

import com.printhelloworld.hobby.entity.Hobby;

public interface HobbyRedisRepository extends CrudRepository<Hobby, Long> {

}
