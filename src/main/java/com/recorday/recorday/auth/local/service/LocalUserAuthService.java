package com.recorday.recorday.auth.local.service;

import com.recorday.recorday.auth.local.dto.request.LocalRegisterRequest;

public interface LocalUserAuthService {

	void register(LocalRegisterRequest request);

	boolean isExistLocal(String email);
}
