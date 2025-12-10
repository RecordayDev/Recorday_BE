package com.recorday.recorday.storage.strategy;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.recorday.recorday.storage.enums.UploadType;

@Component
public class FourcutUploadPathStrategy implements UploadPathStrategy{

	@Override
	public UploadType getUploadType() {
		return UploadType.FOURCUT;
	}

	@Override
	public String generateKey(String publicId, String originalFilename, boolean isTemp) {

		String extension = extractExtension(originalFilename);
		String uniqueName = UUID.randomUUID() + extension;

		return String.format("uploads/users/%s/fourcuts/%s", publicId, uniqueName);
	}
}
