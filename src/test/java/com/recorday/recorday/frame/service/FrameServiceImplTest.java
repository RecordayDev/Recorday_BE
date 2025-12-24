package com.recorday.recorday.frame.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.frame.component.FrameAssetManager;
import com.recorday.recorday.frame.component.FrameStyleConverter;
import com.recorday.recorday.frame.dto.request.FrameCreateRequest;
import com.recorday.recorday.frame.entity.Frame;
import com.recorday.recorday.frame.entity.attributes.ColorBackgroundAttributes;
import com.recorday.recorday.frame.enums.ComponentType;
import com.recorday.recorday.frame.enums.FrameType;
import com.recorday.recorday.frame.repository.FrameRepository;
import com.recorday.recorday.storage.service.S3FileStorageService;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserRole;
import com.recorday.recorday.user.enums.UserStatus;
import com.recorday.recorday.util.user.UserReader;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class FrameServiceImplTest {

	@Mock
	private FrameRepository frameRepository;

	@Mock
	private UserReader userReader;

	@Mock
	private FrameAssetManager frameAssetManager;

	@Mock
	private FrameStyleConverter frameStyleConverter;

	@InjectMocks
	private FrameServiceImpl frameService;

	@Test
	@DisplayName("프레임 생성 시 자원 이동 및 스타일 변환이 정상적으로 협력 객체에 위임된다.")
	void createFrame_Success() {
		//given
		Long userId = 1L;
		String tempKey = "temp/photo.jpg";
		String permanentKey = "uploads/users/uuid/assets/original/uuid.jpg";
		String expectedStyleJson = "{\"borderRadius\": 10}";

		User mockUser = createUser("test@test.com");

		var componentReq = new FrameCreateRequest.ComponentRequest(
			"uuid-1", ComponentType.PHOTO, tempKey,
			10.0, 20.0, 100.0, 100.0, 0.0, 1, Map.of("borderRadius", 10)
		);

		var request = new FrameCreateRequest(
			"My Frame", "desc1", "frame_base_key", FrameType.CLASSIC,
			800, 1200,
			new ColorBackgroundAttributes("#FFFFFF"),
			List.of(componentReq)
		);

		given(userReader.getUserById(userId)).willReturn(mockUser);
		given(frameAssetManager.moveTempFilesToPermanent(mockUser, request.components()))
			.willReturn(Map.of(tempKey, permanentKey));
		given(frameStyleConverter.convertToJson(any())).willReturn(expectedStyleJson);

		//when
		frameService.createFrame(userId, request);

		//then
		then(userReader).should(times(1)).getUserById(userId);
		then(frameAssetManager).should(times(1)).moveTempFilesToPermanent(mockUser, request.components());
		then(frameStyleConverter).should(times(1)).convertToJson(componentReq.style());

		ArgumentCaptor<Frame> frameCaptor = ArgumentCaptor.forClass(Frame.class);
		then(frameRepository).should(times(1)).save(frameCaptor.capture());

		Frame savedFrame = frameCaptor.getValue();

		assertThat(savedFrame.getComponents()).hasSize(1);
		assertThat(savedFrame.getComponents().get(0).getSource()).isEqualTo(permanentKey);
		assertThat(savedFrame.getComponents().get(0).getStyleJson()).isEqualTo(expectedStyleJson);
		assertThat(savedFrame.getUser()).isEqualTo(mockUser);
	}

	private User createUser(String email) {
		return User.builder()
			.id(1L)
			.publicId("user-public-id-123")
			.email(email)
			.username("tester")
			.profileUrl("http://profile.url")
			.provider(Provider.RECORDAY)
			.userRole(UserRole.ROLE_USER)
			.userStatus(UserStatus.ACTIVE)
			.build();
	}

}