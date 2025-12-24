package com.recorday.recorday.frame.entity.attributes;

import com.recorday.recorday.frame.enums.BackgroundType;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ColorBackgroundAttributes extends BackgroundAttributes{
	private String value;

	public ColorBackgroundAttributes(String value) {
		this.type = BackgroundType.COLOR;
		this.value = value;
	}
}
