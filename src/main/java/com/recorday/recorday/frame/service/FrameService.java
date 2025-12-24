package com.recorday.recorday.frame.service;

import com.recorday.recorday.frame.dto.request.FrameCreateRequest;

public interface FrameService {

	void createFrame(Long userId, FrameCreateRequest request);
}
