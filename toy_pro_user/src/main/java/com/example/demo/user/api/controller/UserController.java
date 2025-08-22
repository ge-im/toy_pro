package com.example.demo.user.api.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.user.api.dto.UserCreateRequestDTO;
import com.example.demo.user.api.dto.UserResponseDTO;
import com.example.demo.user.api.dto.UserSearchRequestDTO_before;
import com.example.demo.user.api.dto.UserUpdateRequestDTO;
import com.example.demo.user.service.UserService_before;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * @packageName    : com.example.demo.user.api.controller
 * @fileName       : UserController.java
 * @author         : imge
 * @date           : 2025. 8. 12. 오후 4:24:35
 * @description    : 사용자 기능의 controller
 * 					추후 router, handler로 변경하면서 사용하지 않을 예정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 8. 12.        imge       최초 생성
 */
//@RestController
//@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
	
	private final UserService_before userService;
	
	@GetMapping
	public Flux<UserResponseDTO> search(@ModelAttribute UserSearchRequestDTO_before dto) {
		return userService.search(dto);
	}
	
	@GetMapping("/{userSn}")
	public Mono<UserResponseDTO> findById(@PathVariable long userSn) {
		return userService.findById(userSn);
	}
	
	@PostMapping
	public Mono<UserResponseDTO> create(@RequestBody UserCreateRequestDTO dto) {
		//return 타입에 대해서 고민해보기
		return userService.create(dto);
	}
	
	@PutMapping
	public Mono<UserResponseDTO> update(@RequestBody UserUpdateRequestDTO dto) {
		//return 타입에 대해서 고민해보기
		return userService.update(dto);
	}
	
	@DeleteMapping("/{userSn}")
	public Mono<Void> delete(@PathVariable long userSn) {
		//return 타입에 대해서 고민해보기
		return userService.delete(userSn);
	}
	
}
