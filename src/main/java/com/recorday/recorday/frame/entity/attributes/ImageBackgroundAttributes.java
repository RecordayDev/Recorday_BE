package com.recorday.recorday.frame.entity.attributes;

import com.recorday.recorday.frame.enums.BackgroundType;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class ImageBackgroundAttributes extends BackgroundAttributes {
	private String key;
	private double opacity;

	public ImageBackgroundAttributes(String key, double opacity) {
		this.type = BackgroundType.IMAGE;
		this.key = key;
		this.opacity = opacity;
	}
}
