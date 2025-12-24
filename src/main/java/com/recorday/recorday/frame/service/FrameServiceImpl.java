package com.recorday.recorday.frame.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recorday.recorday.frame.component.FrameAssetManager;
import com.recorday.recorday.frame.component.FrameStyleConverter;
import com.recorday.recorday.frame.dto.request.FrameCreateRequest;
import com.recorday.recorday.frame.entity.Frame;
import com.recorday.recorday.frame.entity.FrameComponent;
import com.recorday.recorday.frame.repository.FrameRepository;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.util.user.UserReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FrameServiceImpl implements FrameService {

	private final FrameRepository frameRepository;
	private final UserReader userReader;
	private final FrameAssetManager frameAssetManager;
	private final FrameStyleConverter frameStyleConverter;

	@Override
	public void createFrame(Long userId, FrameCreateRequest request) {

		User user = userReader.getUserById(userId);

		Map<String, String> resolvedUrlMap = frameAssetManager.moveTempFilesToPermanent(user, request.components());

		Frame frame = Frame.builder()
			.title(request.title())
			.description(request.description())
			.source(request.key())
			.frameType(request.frameType())
			.canvasWidth(request.canvasWidth())
			.canvasHeight(request.canvasHeight())
			.background(request.background())
			.build();

		frame.assignUser(user);

		for (var componentDto : request.components()) {

			String finalSource = resolvedUrlMap.getOrDefault(componentDto.key(), componentDto.key());

			String styleJson = frameStyleConverter.convertToJson(componentDto.style());

			FrameComponent component = FrameComponent.builder()
				.type(componentDto.type())
				.source(finalSource)
				.x(componentDto.x())
				.y(componentDto.y())
				.width(componentDto.width())
				.height(componentDto.height())
				.rotation(componentDto.rotation())
				.zIndex(componentDto.zIndex())
				.styleJson(styleJson)
				.build();

			frame.addComponent(component);
		}
		frameRepository.save(frame);
	}
}
