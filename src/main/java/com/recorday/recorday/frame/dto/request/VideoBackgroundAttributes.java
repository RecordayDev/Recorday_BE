package com.recorday.recorday.frame.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
class VideoBackgroundAttributes extends BackgroundAttributes {
	private String url;
	private boolean autoPlay;
	private boolean loop;
}
