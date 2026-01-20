package com.example.demo.like.service;

import org.springframework.stereotype.Service;

import com.example.demo.like.domain.repository.LikeHistoryRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LikeService {
	//redis 연결 template
	//likeHistoryRespository
	private final LikeHistoryRepository repository;

	//api dto가 필요한지 고민...
	//키값의 역할 target_type, target_sn, user_sn 
	
	public Mono<Boolean> checkLike(String targetType, long targetSn, long userSn) {
		//redis 단건 존재 여부 조회
		return null;
	}
	
	public Mono<Long> countLike(String targetType, long targetSn) {
		//redis 실시간 개수 조회
		return null;
	}
	
	public Flux<Long> findUserIdsByLikes(String targetType, long targetSn) {
		//redis 조건 목록 조회
		return null;
	}
	
	public Mono<Void> doLike(String targetType, long targetSn, long userSn) {
		//redis 단건 존재 여부 조회
		//존재한다면 오류(200번대 response중 적합한 걸로 return)
		//존재하지 않으면 redis에 insert + 결과 확인 후 t_like_h01에 insert
		//두가지 모두 성공해야 200 return
		return null;
	}
	
	public Mono<Void> undoLike(String targetType, long targetSn, long userSn) {
		//redis 단건 존재 여부 조회
		//존재하지 않으면 오류(200번대 response중 적합한 걸로 return)
		//존재하면 redis에 delete + 결과 확인 후 t_like_h01에 insert
		return null;
	}
}
