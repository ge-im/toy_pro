package com.example.demo.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.web.reactive.function.server.ServerRequest;

import com.example.demo.common.dto.PageableDTO;
import com.example.demo.common.error.code.BusinessErrorCode;
import com.example.demo.common.error.exception.BusinessException;

public class RequestParameterUtil {
	
	private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	public static LocalDateTime getRequiredQueryParamAsDateTime(ServerRequest req, String name) {
        return req.queryParam(name)
				  .map(param -> parseDateTime(param))
				  .orElseThrow(() -> invalidRequiredParameter(name));
    }
	
	public static LocalDateTime getQueryParamAsDateTime(ServerRequest req, String name) {
		return req.queryParam(name)
				  .map(RequestParameterUtil::parseDateTime)
				  .orElse(null);
	}
	
	public static PageableDTO getRequirdPageableDTO(ServerRequest req) {
		return PageableDTO.builder()
				.page(checkValue(req, "page", PageableDTO.DEFAULT_PAGE, true))
				.size(checkValue(req, "size", PageableDTO.DEFAULT_SIZE, true))
				.build();
	}
	
	public static PageableDTO getPageableDTO(ServerRequest req) {
		return PageableDTO.builder()
						  .page(checkValue(req, "page", PageableDTO.DEFAULT_PAGE, false))
						  .size(checkValue(req, "size", PageableDTO.DEFAULT_SIZE, false))
						  .build();
	}
	
	private static int checkValue(ServerRequest req, String name, int defaultVal, boolean isRequired) {
		int result = defaultVal;
		
		try {
			if(isRequired)
				result = req.queryParam(name)
							.map(param -> Integer.parseInt(param))
							.orElseThrow(() -> invalidRequiredParameter(name));
			else 
				result = req.queryParam(name)
							.map(param -> Integer.parseInt(param))
							.orElse(defaultVal);
		} catch (Exception e) {
			if(e instanceof BusinessException)
				throw e;
			
			//파싱에러로 추정
			//올바르지 않은 형식(숫자가 아닌 형식)으로 파라미터를 넘긴 경우 - 고의성이 있기 때문에 오류 처리
			throw new BusinessException(BusinessErrorCode.INVALID_REQUEST_PARAMETER);
		}
		
		return result < 0 ? defaultVal : result;
	}

	private static LocalDateTime parseDateTime(String value) {
		try {
			return LocalDateTime.parse(value, DEFAULT_FORMATTER);
		} catch (RuntimeException e) {
			throw new BusinessException(BusinessErrorCode.INVALID_REQUEST_PARAMETER);
		}
	}

	private static BusinessException invalidRequiredParameter(String name) {
		return new BusinessException(BusinessErrorCode.INVALID_REQUEST_PARAMETER, name + " is required");
	}
}
