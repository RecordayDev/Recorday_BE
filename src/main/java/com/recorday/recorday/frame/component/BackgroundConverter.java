package com.recorday.recorday.frame.component;

import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.exception.GlobalErrorCode;
import com.recorday.recorday.frame.entity.attributes.BackgroundAttributes;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Converter
public class BackgroundConverter implements AttributeConverter<BackgroundAttributes, String> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(BackgroundAttributes attribute) {
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (JacksonException e) {
			throw new BusinessException(GlobalErrorCode.JSON_PARSE_ERROR);
		}
	}

	@Override
	public BackgroundAttributes convertToEntityAttribute(String dbData) {
		try {
			return objectMapper.readValue(dbData, BackgroundAttributes.class);
		} catch (JacksonException e) {
			throw new BusinessException(GlobalErrorCode.JSON_PARSE_ERROR);
		}
	}
}
