package com.recorday.recorday.user.service;

import com.recorday.recorday.user.dto.response.UserInfoResponse;

public interface UserService {

	UserInfoResponse getUserInfo(Long userId);

	void changeUsername(Long userId, String username);

	void changeProfileImage(Long userId, String s3Key);
}
