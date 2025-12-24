package com.recorday.recorday.frame.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.exception.GlobalErrorCode;
import com.recorday.recorday.frame.dto.request.FrameCreateRequest;
import com.recorday.recorday.frame.enums.BackgroundType;
import com.recorday.recorday.frame.enums.ComponentType;
import com.recorday.recorday.storage.service.FileStorageService;
import com.recorday.recorday.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FrameAssetManager {

	private final FileStorageService fileStorageService;
	private static final String TEMP_ROOT = "temp";
	private static final String UPLOAD_ROOT = "uploads";

	public Map<String, String> moveTempFilesToPermanent(User user,
		List<FrameCreateRequest.ComponentRequest> components) {

		String userTempPathCheck = String.format("%s/users/%s/", TEMP_ROOT, user.getPublicId());

		var tempKeys = components.stream()
			.filter(c -> c.type() == ComponentType.PHOTO)
			.map(FrameCreateRequest.ComponentRequest::source)
			.filter(source -> source != null && source.startsWith(userTempPathCheck))
			.collect(Collectors.toSet());

		if (tempKeys.isEmpty()) {
			return Map.of();
		}

		Map<String, String> keyMapping = new HashMap<>();

		for (String tempKey : tempKeys) {
			try {
				String targetKey = tempKey.replaceFirst(TEMP_ROOT, UPLOAD_ROOT);

				fileStorageService.moveFile(tempKey, targetKey);
				keyMapping.put(tempKey, targetKey);
			} catch (Exception e) {
				log.error("Failed to move file from temp: {}", tempKey);
				throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "이미지 저장 중 오류가 발생했습니다.");
			}
		}
		return keyMapping;
	}

	public String moveTempFileToPermanent(User user, String tempKey) {
		String userTempPathCheck = String.format("%s/users/%s/", TEMP_ROOT, user.getPublicId());

		if (tempKey == null || !tempKey.startsWith(userTempPathCheck)) {
			return tempKey;
		}

		try {
			String targetKey = tempKey.replaceFirst(TEMP_ROOT, UPLOAD_ROOT);

			fileStorageService.moveFile(tempKey, targetKey);
			return targetKey;
		} catch (Exception e) {
			log.error("배경 파일 이동 실패: {}", tempKey);
			throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "배경 파일 저장 중 오류 발생");
		}
	}

	public String resolveSource(ComponentType type, String source) {
		if (source == null)
			return null;

		return switch (type) {
			case PHOTO -> fileStorageService.generatePresignedGetUrl(source);
			default -> source;
		};
	}

	public String resolveSource(BackgroundType type, String source) {
		if (source == null)
			return null;

		return switch (type) {
			case IMAGE, VIDEO -> fileStorageService.generatePresignedGetUrl(source);
			default -> source;
		};
	}

	public void deleteFiles(List<String> keys) {
		for (String key : keys) {
			if (key != null && !key.isBlank()) {
				fileStorageService.delete(key);
			}
		}
	}
}

