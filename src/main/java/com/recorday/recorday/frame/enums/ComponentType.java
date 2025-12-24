package com.recorday.recorday.frame.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComponentType {

	PHOTO("사진", "사용자가 업로드한 사진"),
	STICKER("스티커", "제공되는 데코레이션 스티커"),
	TEXT("텍스트", "사용자 입력 텍스트");

	private final String title;
	private final String description;
}
