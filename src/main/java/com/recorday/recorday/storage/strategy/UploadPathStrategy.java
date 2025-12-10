package com.recorday.recorday.storage.strategy;

import com.recorday.recorday.storage.enums.UploadType;

public interface UploadPathStrategy {

	UploadType getUploadType();

	String generateKey(String publicId, String originalFilename, boolean isTemp);

	default String extractExtension(String originalFilename) {
		int index = originalFilename.lastIndexOf(".");
		return (index > 0) ? originalFilename.substring(index) : "";
	}
}
