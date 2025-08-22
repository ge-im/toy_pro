package com.example.demo.user.service;

import org.springframework.stereotype.Service;

import com.example.demo.user.api.dto.UserCreateRequestDTO;
import com.example.demo.user.api.dto.UserResponseDTO;
import com.example.demo.user.api.dto.UserSearchRequestDTO_before;
import com.example.demo.user.api.dto.UserUpdateRequestDTO;
import com.example.demo.user.domain.mapper.UserMapper;
import com.example.demo.user.domain.model.User;
import com.example.demo.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @packageName    : com.example.demo.user.domain.service
 * @fileName       : UserService_before.java
 * @author         : imge
 * @date           : 2025. 8. 12. 오후 4:27:33
 * @description    : 사용자 기능의 business 레이어 - controller 사용 버전(페이징 적용 이전)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 8. 12.        imge       최초 생성
 */
@Service
@RequiredArgsConstructor
public class UserService_before {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	
	public Flux<UserResponseDTO> search(UserSearchRequestDTO_before dto) {
		return userRepository.findAll(dto.userNm().get(), dto.size().get(), dto.getOffset())
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
		
		//id 체크 한번 더
		//id가 없으면 저장으로 변경
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
