package com.example.demo.auth.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
	
	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = -5943177880944569122L;
	
	private final String token;
	
	public JwtAuthenticationToken(String token) {
		super(null);
		this.token = token;
		setAuthenticated(false);
	}

	@Override
	public Object getCredentials() {
		return token;
	}

	@Override
	public Object getPrincipal() {
		return null; 
	}
}
