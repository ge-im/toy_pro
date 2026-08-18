package com.example.demo.config.error;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.example.demo.common.error.code.ErrorCode;
import com.example.demo.common.error.code.HttpErrorCode;
import com.example.demo.common.error.exception.BusinessException;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

	private final ErrorResponseFactory errorResponseFactory;

	public GlobalErrorAttributes(ErrorResponseFactory errorResponseFactory) {
		this.errorResponseFactory = errorResponseFactory;
	}
	
	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		Throwable e = getError(request);
		ErrorCode errorCode = resolveErrorCode(e);
		return errorResponseFactory.create(errorCode, resolveMessage(e), request.path());
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
