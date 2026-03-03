package com.example.demo.auth.domain.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.auth.domain.model.AuthRole;

import reactor.core.publisher.Flux;

@Repository
public interface AuthRoleRepository extends ReactiveCrudRepository<AuthRole, Long> {
	//상위->하위 쿼리(하위 권한들 모두 나옴)
	@Query("""
			WITH RECURSIVE role_tree AS (
			    SELECT r.role_sn, r.role_cd
			    FROM t_role_m01 r
			    	INNER JOIN t_user_role_s01 ur 
			    		ON ur.role_sn = r.role_sn
			    WHERE ur.user_sn = :userSn
			
			    UNION ALL
			
			    SELECT child.role_sn, child.role_cd
			    FROM t_role_m01 child
				    INNER JOIN t_role_hierarchy_s01 rh 
				    	ON rh.child_role_sn = child.role_sn
			    	INNER JOIN role_tree parent 
			    		ON rh.parent_role_sn = parent.role_sn
			)
			SELECT DISTINCT role_cd FROM role_tree;
			""")
	public Flux<String> findAllByUserId(long userSn);

}
