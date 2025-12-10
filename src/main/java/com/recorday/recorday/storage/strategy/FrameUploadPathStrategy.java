package com.recorday.recorday.storage.strategy;

import java.util.UUID;

import com.recorday.recorday.storage.enums.UploadType;

public class FrameUploadPathStrategy implements UploadPathStrategy{

	@Override
	public UploadType getUploadType() {
		return UploadType.FRAME;
	}

	@Override
	public String generateKey(Long userId, String originalFilename) {
		String extension = extractExtension(originalFilename);
		String uniqueName = UUID.randomUUID() + extension;

		return String.format("uploads/users/%d/frames/%s", userId, uniqueName);
	}
}
