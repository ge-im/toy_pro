package com.example.demo.user.service;

import org.springframework.stereotype.Service;

import com.example.demo.common.dto.PageableDTO;
import com.example.demo.common.dto.SearchDTO;
import com.example.demo.common.error.code.BusinessErrorCode;
import com.example.demo.common.error.exception.BusinessException;
import com.example.demo.user.api.dto.UserCreateRequestDTO;
import com.example.demo.user.api.dto.UserResponseDTO;
import com.example.demo.user.api.dto.UserSearchRequestDTO;
import com.example.demo.user.api.dto.UserUpdateRequestDTO;
import com.example.demo.user.domain.mapper.UserMapper;
import com.example.demo.user.domain.model.User;
import com.example.demo.user.domain.repository.UserCustomRepository;
import com.example.demo.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @packageName    : com.example.demo.user.domain.service
 * @fileName       : UserService.java
 * @author         : imge
 * @date           : 2025. 8. 12. 오후 4:27:33
 * @description    : 사용자 기능의 business 레이어 -router/handler, 페이징 적용
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 8. 12.        imge       최초 생성
 */
@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserRepository userRepository;
	private final UserCustomRepository userCustomRepository;
	private final UserMapper userMapper;
	
	public Flux<UserResponseDTO> findAll(String userNm, PageableDTO dto){
		return userRepository.findAll(userNm, dto.getSize(), dto.getSize() * dto.getPage())
							 .map(userMapper::toResponse);
	}
	
	public Flux<UserResponseDTO> findAllByConditions(SearchDTO<UserSearchRequestDTO> dto) {
		return userCustomRepository.findAllByConditions(dto)
								   .map(userMapper::toResponse);
	}
	
	public Mono<UserResponseDTO> findById(Long userSn) {
		return userRepository.findById(userSn).map(userMapper::toResponse);
	}
	
	public Mono<Long> create(UserCreateRequestDTO dto) {
		User u = userMapper.toEntity(dto);
		u.setDelYn("N");
		u.setRegDt(java.time.LocalDateTime.now());
		u.setUpdtDt(java.time.LocalDateTime.now());
		
		//id 중복체크 할지 안할지 정하기, unique key 제약을 두면 오류가 나긴함
		// ==>> 정책 정하기
		return userRepository.save(u)
							 .map(r -> r.getUserSn());
	}
	
	public Mono<Void> update(UserUpdateRequestDTO dto) {
		return userRepository.findById(dto.userSn())
					  .filter(u -> "N".equals(u.getDelYn()))
					  .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.OBJECT_NOT_FOUND)))
					  .flatMap(u -> {
						  userMapper.updateEntityFromDto(dto, u);
						  return userRepository.save(u);
					  })
					  .then();
	}
	
	public Mono<Void> delete(long userSn) {
		return userRepository.findById(userSn)
					.filter(u -> "N".equals(u.getDelYn()))
					.switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.OBJECT_NOT_FOUND)))
					.flatMap(u -> {
						u.setDelYn("Y");
						u.setUpdtDt(java.time.LocalDateTime.now());
						return userRepository.save(u);
					})
					.then();
	}
}
