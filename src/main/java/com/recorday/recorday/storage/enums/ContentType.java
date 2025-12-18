package com.recorday.recorday.storage.enums;

import java.util.Arrays;
import java.util.Set;

import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.exception.GlobalErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentType {

	// Image
	JPEG("image/jpeg", Set.of("jpg", "jpeg"), MediaType.IMAGE),
	PNG("image/png", Set.of("png"), MediaType.IMAGE),
	WEBP("image/webp", Set.of("webp"), MediaType.IMAGE),
	GIF("image/gif", Set.of("gif"), MediaType.IMAGE),

	// Video
	MP4("video/mp4", Set.of("mp4"), MediaType.VIDEO),
	WEBM("video/webm", Set.of("webm"), MediaType.VIDEO),
	MOV("video/quicktime", Set.of("mov"), MediaType.VIDEO);

	private final String mimeType;
	private final Set<String> extensions;
	private final MediaType mediaType;

	public enum MediaType {
		IMAGE, VIDEO
	}

	public static ContentType validate(String mimeType, String extension) {
		String ext = normalizeExt(extension);

		return Arrays.stream(values())
			.filter(type ->
				type.mimeType.equalsIgnoreCase(mimeType) &&
					type.extensions.contains(ext)
			)
			.findFirst()
			.orElseThrow(() -> new BusinessException(
				GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE,
				"지원하지 않는 MIME/확장자: " + mimeType + " / " + ext
			));
	}

	private static String normalizeExt(String extension) {
		if (extension == null || extension.isBlank()) {
			throw new BusinessException(GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE, "확장자가 비어있습니다.");
		}
		String ext = extension.trim();
		if (ext.startsWith(".")) ext = ext.substring(1);
		return ext.toLowerCase();
	}
}

