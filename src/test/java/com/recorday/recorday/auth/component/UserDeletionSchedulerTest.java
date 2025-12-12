package com.recorday.recorday.auth.component;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.recorday.recorday.auth.batch.UserExitBatchService;
import com.recorday.recorday.exception.BusinessException;
import com.recorday.recorday.user.enums.UserStatus;
import com.recorday.recorday.user.exception.UserErrorCode;
import com.recorday.recorday.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDeletionSchedulerTest {

	private UserDeletionScheduler scheduler;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserExitBatchService userExitBatchService;

	@Mock
	private Clock clock;

	@BeforeEach
	void setUp() {
		scheduler = new UserDeletionScheduler(userRepository, userExitBatchService, clock);
	}

	@Test
	@DisplayName("7일이 지난 탈퇴 요청 회원을 조회하여 일괄 삭제 처리한다")
	void run_success() {
		//given
		Instant fixedInstant = Instant.parse("2025-12-10T00:00:00Z");

		given(clock.instant()).willReturn(fixedInstant);
		given(clock.getZone()).willReturn(ZoneId.systemDefault());

		List<Long> targetIds = List.of(1L, 2L, 3L);

		given(userRepository.findExpiredDeleteRequestedUserIds(
			eq(UserStatus.DELETED_REQUESTED),
			any(LocalDateTime.class))
		).willReturn(targetIds);

		//when
		scheduler.run();

		//then
		then(userExitBatchService).should(times(1)).exitInNewTransaction(1L);
		then(userExitBatchService).should(times(1)).exitInNewTransaction(2L);
		then(userExitBatchService).should(times(1)).exitInNewTransaction(3L);
	}

	@Test
	@DisplayName("처리 중 예외가 발생해도 다음 회원의 처리는 계속되어야 한다")
	void run_Continues_OnException() {
		//given
		Instant fixedInstant = Instant.parse("2025-12-10T00:00:00Z");
		given(clock.instant()).willReturn(fixedInstant);
		given(clock.getZone()).willReturn(ZoneId.systemDefault());

		List<Long> targetIds = List.of(1L, 2L, 3L);

		given(userRepository.findExpiredDeleteRequestedUserIds(any(), any()))
			.willReturn(targetIds);

		lenient()
			.doThrow(new BusinessException(UserErrorCode.NOT_TARGET))
			.when(userExitBatchService).exitInNewTransaction(2L);

		//when
		scheduler.run();

		//then
		then(userExitBatchService).should(times(1)).exitInNewTransaction(1L);
		then(userExitBatchService).should(times(1)).exitInNewTransaction(2L);
		then(userExitBatchService).should(times(1)).exitInNewTransaction(3L);
	}

}