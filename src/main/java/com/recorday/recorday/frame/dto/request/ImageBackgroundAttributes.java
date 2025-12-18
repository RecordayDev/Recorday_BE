package com.recorday.recorday.frame.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

class ImageBackgroundAttributes extends BackgroundAttributes {
	private String url;     // S3 URL
	private double opacity; // 투명도
}
