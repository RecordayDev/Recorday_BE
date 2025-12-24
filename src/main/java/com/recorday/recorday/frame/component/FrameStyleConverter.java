package com.recorday.recorday.frame.component;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.exception.GlobalErrorCode;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class FrameStyleConverter {

	private final ObjectMapper objectMapper;

	public String convertToJson(Map<String, Object> styleMap) {
		if (styleMap == null || styleMap.isEmpty()) {
			return "{}";
		}
		try {
			return objectMapper.writeValueAsString(styleMap);
		} catch (Exception e) { // JacksonException 등을 포괄
			throw new BusinessException(GlobalErrorCode.JSON_PARSE_ERROR, "스타일 데이터 변환 중 오류가 발생했습니다.");
		}
	}
}
