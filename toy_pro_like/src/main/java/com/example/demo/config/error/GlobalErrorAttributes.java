package com.example.demo.config.error;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.example.demo.common.error.code.ErrorCode;
import com.example.demo.common.error.code.HttpErrorCode;
import com.example.demo.common.error.exception.BusinessException;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {
	
	@Override
	public Map<String, @Nullable Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		Throwable e = getError(request);
		
		Map<String, Object> attributes = new LinkedHashMap<>();
		
		ErrorCode errorCode = resolveErrorCode(e);
		attributes.put("status", errorCode.getHttpStatus().value());
		attributes.put("code", errorCode.getCode());
		attributes.put("message", resolveMessage(e));
		attributes.put("time", LocalDateTime.now().toString());
		attributes.put("path", request.path());
		
		return attributes;
	}
	
	private ErrorCode resolveErrorCode(Throwable error) {
		
		if(error instanceof BusinessException be)
			return be.getErrorCode();
		
		//error처리 목록 정리 필요
		
		return HttpErrorCode.INTERVAL_SERVER_ERROR;
	}
	
	//필요한가?
//	private String getTraceId( ) {
//		return MDC.get("traceId");
//	}
	
	private String resolveMessage(Throwable e) {
		return StringUtils.hasText(e.getMessage()) ? e.getMessage() : "Unexpected error occured";
	}
}
