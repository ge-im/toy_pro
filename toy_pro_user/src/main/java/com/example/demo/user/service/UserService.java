package com.example.demo.user.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.user.api.dto.UserCreateRequestDTO;
import com.example.demo.user.api.dto.UserResponseDTO;
import com.example.demo.user.api.dto.UserSearchRequestDTO;
import com.example.demo.user.api.dto.UserUpdateRequestDTO;
import com.example.demo.user.domain.mapper.UserMapper;
import com.example.demo.user.domain.model.User;
import com.example.demo.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @packageName    : com.example.demo.user.domain.service
 * @fileName       : UserService.java
 * @author         : imge
 * @date           : 2025. 8. 12. 오후 4:27:33
 * @description    : 사용자 기능의 business 레이어
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 8. 12.        imge       최초 생성
 */
@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	
	public Flux<UserResponseDTO> search(UserSearchRequestDTO dto) {
		return userRepository.findActiveUser(dto.userNm().get(), dto.size().get(), dto.getOffset())
							 .map(userMapper::toResponse);
	}
	
	public Mono<UserResponseDTO> findById(Long userSn) {
		return userRepository.findById(userSn).map(userMapper::toResponse);
	}
	
	public Mono<UserResponseDTO> create(UserCreateRequestDTO dto) {
		User u = userMapper.toEntity(dto);
		u.setDelYn("N");
		u.setRegDt(java.time.LocalDateTime.now());
		u.setUpdtDt(java.time.LocalDateTime.now());
		
		return userRepository.save(u)
							 .map(userMapper::toResponse);
	}
	
	public Mono<UserResponseDTO> update(UserUpdateRequestDTO dto) {
		return userRepository.findById(dto.userSn())
					  .filter(u -> "N".equals(u.getDelYn()))
//					  .switchIfEmpty(Mono.error(new user)) //사용자 없음(오류)
					  .flatMap(u -> {
						  userMapper.updateEntityFromDto(dto, u);
						  return userRepository.save(u);
					  })
					  .map(userMapper::toResponse);
	}
	
	public Mono<Void> delete(long userSn) {
		return userRepository.findById(userSn)
					.filter(u -> "N".equals(u.getDelYn()))
//					.switchIfEmpty(Mono.error(new user)) //사용자 없음(오류)
					.flatMap(u -> {
						u.setDelYn("Y");
						u.setUpdtDt(java.time.LocalDateTime.now());
						return userRepository.save(u);
					})
					.then();
	}
}
