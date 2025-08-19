package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.r2dbc.proxy.ProxyConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;

//@Configuration
public class R2dbcProxyConfig {
	
	
//	@Bean
//	public ConnectionFactory connectionFactory(ConnectionFactory connectionFactory) {
//		return ProxyConnectionFactory.builder(connectionFactory).listener(new InlineQueryLoggingListener).build();
//	}
}
