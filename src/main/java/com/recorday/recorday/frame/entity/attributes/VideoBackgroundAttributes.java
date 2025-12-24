package com.recorday.recorday.frame.entity.attributes;

import com.recorday.recorday.frame.enums.BackgroundType;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VideoBackgroundAttributes extends BackgroundAttributes {
	private String key;
	private boolean autoPlay;
	private boolean loop;

	public VideoBackgroundAttributes(String key, boolean autoPlay, boolean loop) {
		this.type = BackgroundType.VIDEO;
		this.key = key;
		this.autoPlay = autoPlay;
		this.loop = loop;
	}
}
