package com.example.demo.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.web.reactive.function.server.ServerRequest;

import com.example.demo.common.dto.PageableDTO;

public class RequestParameterUtil {
	
	private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	public static LocalDateTime getRequiredQueryParamAsDateTime(ServerRequest req, String name) {
        return req.queryParam(name)
                  .map(param -> LocalDateTime.parse(param, DEFAULT_FORMATTER))
                  .orElseThrow(() -> new IllegalArgumentException(name + " is required"));
    }
	
	public static LocalDateTime getQueryParamAsDateTime(ServerRequest req, String name) {
		return req.queryParam(name)
				  .map(param -> LocalDateTime.parse(param, DEFAULT_FORMATTER))
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
							.orElseThrow(() -> new IllegalArgumentException(name + " is required"));
			else 
				result = req.queryParam(name)
							.map(param -> Integer.parseInt(param))
							.orElse(defaultVal);
		} catch (Exception e) {
			if(e instanceof IllegalArgumentException)
				throw e;
			
			//파싱에러로 추정
			//올바르지 않은 형식(숫자가 아닌 형식)으로 파라미터를 넘긴 경우 - 고의성이 있기 때문에 오류 처리
			throw new IllegalArgumentException("wrong argument, " + name);
		}
		
		return result < 0 ? defaultVal : result;
	}
}
