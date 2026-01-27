package com.example.demo.like.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.demo.common.exception.AlreadyLikedExcepction;
import com.example.demo.common.exception.ObjectNotFoundException;
import com.example.demo.like.domain.model.LikeHistory;
import com.example.demo.like.domain.repository.LikeHistoryRepository;
import com.example.demo.like.domain.repository.LikeRedisRepository;
import com.example.demo.like.enums.LikeActionType;
import com.example.demo.like.enums.TargetType;
import com.example.demo.like.infra.redis.LikeRedisKeyGenerator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LikeService {
	
	private final LikeRedisRepository redisRepository;
	
	private final LikeHistoryRepository historyRepository;
	
	private final LikeRedisKeyGenerator keyGenerator;

	public Mono<Boolean> isLiked(TargetType targetType, long targetSn, long userSn) {
		return redisRepository.isLiked(keyGenerator.generate(targetType, targetSn), userSn);
	}
	
	public Mono<Long> countLikes(TargetType targetType, long targetSn) {
		return redisRepository.countLikes(keyGenerator.generate(targetType, targetSn));
	}
	
	public Flux<Long> findIsLikedUsers(TargetType targetType, long targetSn) {
		return redisRepository.findIsLikedUsers(keyGenerator.generate(targetType, targetSn));
	}
	
	public Mono<Void> like(TargetType targetType, long targetSn, long userSn) {
		String key = keyGenerator.generate(targetType, targetSn);
		
		return redisRepository.isLiked(key, userSn)
							  .flatMap(e -> {
								  if(e)
									  return Mono.error(new AlreadyLikedExcepction("Already Liked"));
								  else
									  return redisRepository.addLike(key, userSn)
									  				 .then(historyRepository.save(getHistory(targetType, targetSn, userSn, LikeActionType.REMOVE)));
									  
							  })
							  .then();
	}
	
	public Mono<Void> unLike(TargetType targetType, long targetSn, long userSn) {
		String key = keyGenerator.generate(targetType, targetSn);
		
		return redisRepository.isLiked(key, userSn)
							  .flatMap(e -> {
								  if(e)
									  return redisRepository.removeLike(key, userSn)
											  		.then(historyRepository.save(getHistory(targetType, targetSn, userSn, LikeActionType.REMOVE)));
								  else
									  return Mono.error(new ObjectNotFoundException("no history for like"));
							  })
							  .then();
	}
	
	private LikeHistory getHistory(TargetType targetType, long targetSn, long userSn, LikeActionType actionType) {
		LikeHistory result = new LikeHistory();
		
		result.setTargetType(targetType.getValue());
		result.setTargetSn(targetSn);
		result.setUserSn(userSn);
		result.setActionType(actionType.getValue());
		result.setRedDt(LocalDateTime.now());
		
		return result;
	}
	
}
