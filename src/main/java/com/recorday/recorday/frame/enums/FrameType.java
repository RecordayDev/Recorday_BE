package com.recorday.recorday.frame.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FrameType {

	CLASSIC("클래식", "2x2 기본 인생네컷 스타일"),
	WIDE("와이드", "가로로 넓은 4컷 스타일"),
	GRID("그리드", "정방형 격자 스타일"),
	POLAROID("폴라로이드", "하단 여백이 있는 폴라로이드 스타일");

	private final String title;
	private final String description;
}
