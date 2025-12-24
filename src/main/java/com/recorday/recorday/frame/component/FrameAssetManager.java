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
	private static final String TEMP_PATH_KEY = "temp/";

	public Map<String, String> moveTempFilesToPermanent(User user,
		List<FrameCreateRequest.ComponentRequest> components) {

		var tempKeys = components.stream()
			.filter(c -> c.type() == ComponentType.PHOTO)
			.map(FrameCreateRequest.ComponentRequest::key)
			.filter(source -> source != null && source.contains(TEMP_PATH_KEY))
			.collect(Collectors.toSet());

		if (tempKeys.isEmpty()) {
			return Map.of();
		}

		Map<String, String> keyMapping = new HashMap<>();
		String baseTargetDir = String.format("uploads/users/%s/assets/original/", user.getPublicId());

		for (String tempKey : tempKeys) {
			try {
				String extension = getExtension(tempKey);
				String newFileName = UUID.randomUUID() + "." + extension;
				String targetKey = baseTargetDir + newFileName;

				fileStorageService.moveFile(tempKey, targetKey);
				keyMapping.put(tempKey, targetKey);
			} catch (Exception e) {
				log.error("Failed to move file from temp: {}", tempKey);
				throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "이미지 저장 중 오류가 발생했습니다.");
			}
		}
		return keyMapping;
	}

	private String getExtension(String filename) {
		return StringUtils.getFilenameExtension(filename);
	}
}

