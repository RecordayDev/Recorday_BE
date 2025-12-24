package com.recorday.recorday.frame.service;

import java.util.List;

import com.recorday.recorday.frame.dto.request.FrameCreateRequest;
import com.recorday.recorday.frame.dto.response.FrameResponse;

public interface FrameService {

	void createFrame(Long userId, FrameCreateRequest request);

	FrameResponse getFrame(Long frameId, Long userId);

	List<FrameResponse> getMyFrame(Long userId);

	void deleteFrame(Long userId, Long frameId);

	void updateFrame(Long userId, Long frameId, FrameCreateRequest request);
}
