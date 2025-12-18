package com.recorday.recorday.frame.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.frame.dto.request.ColorBackgroundAttributes;
import com.recorday.recorday.frame.entity.Frame;
import com.recorday.recorday.frame.entity.FrameComponent;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.user.enums.UserRole;
import com.recorday.recorday.user.enums.UserStatus;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.yml")
class FrameRepositoryTest {

	@Autowired
	private FrameRepository frameRepository;

	@Autowired
	private TestEntityManager em;

	@TestConfiguration
	@EnableJpaAuditing
	static class TestConfig {
	}

	@Test
	@DisplayName("User ID를 기반으로 연관된 FrameComponent를 한 번의 쿼리로 삭제한다.")
	void deleteComponentsByUserIdTest() {
		//given
		User user = createUser("test@test.com");

		Frame frame1 = createFrame("f1", "desc1");
		Frame frame2 = createFrame("f2", "desc2");

		user.addFrame(frame1);
		user.addFrame(frame2);

		frame1.addComponent(FrameComponent.builder()
			.source("src_txt")
			.background(new ColorBackgroundAttributes())
			.build());

		frame2.addComponent(FrameComponent.builder()
			.source("src_img")
			.background(new ColorBackgroundAttributes())
			.build());

		em.persist(user);
		em.flush();
		em.clear();

		//when
		frameRepository.deleteComponentsByUserId(user.getId());

		//then
		Frame findFrame1 = em.find(Frame.class, frame1.getId());
		assertThat(findFrame1.getComponents()).isEmpty();
	}

	@Test
	@DisplayName("User ID를 기반으로 연관된 Frame을 한 번의 쿼리로 삭제한다.")
	void deleteFramesByUserIdTest() {
		// given
		User user = createUser("del@test.com");
		Frame frame = createFrame("delF", "delDesc");

		user.addFrame(frame);

		em.persist(user);
		em.flush();
		em.clear();

		// when
		frameRepository.deleteFramesByUserId(user.getId());

		// then
		User findUser = em.find(User.class, user.getId());
		assertThat(findUser.getFrames()).isEmpty();
	}

	private User createUser(String email) {
		return User.builder()
			.email(email)
			.username("tester")
			.profileUrl("http://profile.url")
			.provider(Provider.RECORDAY)
			.userRole(UserRole.ROLE_USER)
			.userStatus(UserStatus.ACTIVE)
			.build();
	}

	private Frame createFrame(String name, String description) {
		return Frame.builder()
			.name(name)
			.description(description)
			.source("http://frame.source")
			.build();
	}

}