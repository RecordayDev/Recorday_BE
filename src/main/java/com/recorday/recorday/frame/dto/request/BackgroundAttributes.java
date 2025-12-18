package com.recorday.recorday.frame.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.recorday.recorday.frame.enums.BackgroundType;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type",
	visible = true
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ColorBackgroundAttributes.class, name = "COLOR"),
	@JsonSubTypes.Type(value = ImageBackgroundAttributes.class, name = "IMAGE"),
	@JsonSubTypes.Type(value = VideoBackgroundAttributes.class, name = "VIDEO")
})
public abstract class BackgroundAttributes {
	protected BackgroundType type;
}