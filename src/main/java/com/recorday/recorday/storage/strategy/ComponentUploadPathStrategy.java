package com.recorday.recorday.storage.strategy;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.recorday.recorday.storage.enums.UploadType;

@Component
public class ComponentUploadPathStrategy implements UploadPathStrategy{

	private static final String TEMP_PATH_PREFIX = "temp";
	private static final String UPLOAD_PATH_PREFIX = "uploads";

	@Override
	public UploadType getUploadType() {
		return UploadType.COMPONENT;
	}

	@Override
	public String generateKey(String publicId, String originalFilename, boolean isTemp) {
		String rootPath = isTemp ? TEMP_PATH_PREFIX : UPLOAD_PATH_PREFIX;
		String extension = extractExtension(originalFilename);
		String uniqueName = UUID.randomUUID() + extension;

		return String.format("%s/users/%s/components/%s", rootPath, publicId, uniqueName);
	}
}
