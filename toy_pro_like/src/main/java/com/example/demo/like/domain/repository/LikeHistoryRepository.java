package com.example.demo.like.domain.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.example.demo.like.domain.model.LikeHistory;

public interface LikeHistoryRepository extends ReactiveCrudRepository<LikeHistory, Long> {

}
