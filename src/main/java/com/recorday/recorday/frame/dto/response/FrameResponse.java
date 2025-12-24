package com.recorday.recorday.frame.dto.response;

import java.util.List;
import java.util.Map;

import com.recorday.recorday.frame.entity.attributes.BackgroundAttributes;
import com.recorday.recorday.frame.enums.ComponentType;
import com.recorday.recorday.frame.enums.FrameType;

public record FrameResponse(
	Long frameId,
	String title,
	String description,
	String source,
	FrameType frameType,
	int canvasWidth,
	int canvasHeight,
	BackgroundAttributes background,
	List<ComponentResponse> components
) {
	public record ComponentResponse(
		Long id,
		ComponentType type,
		String source,
		double x,
		double y,
		double width,
		double height,
		double rotation,
		int zIndex,
		Map<String, Object> style
	) {}
}
