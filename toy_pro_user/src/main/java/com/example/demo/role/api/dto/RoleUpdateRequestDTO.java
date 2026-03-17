package com.example.demo.role.api.dto;

public record RoleUpdateRequestDTO(
	long roleSn
	, String roleCd
	, String roleNm
) {}
