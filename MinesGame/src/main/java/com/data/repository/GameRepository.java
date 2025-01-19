package com.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.data.entity.Game;

public interface GameRepository extends MongoRepository<Game, String> {
}
