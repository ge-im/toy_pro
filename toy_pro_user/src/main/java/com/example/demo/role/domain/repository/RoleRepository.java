package com.example.demo.role.domain.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.example.demo.role.domain.model.Role;

import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
	
	@Query(" SELECT * FROM t_role_m01 WHERE role_cd = :roleCd ")
	Mono<Role> findByCd(String roleCd);
	
}
