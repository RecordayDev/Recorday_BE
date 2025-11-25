package com.recorday.recorday.auth.local.service;

import com.recorday.recorday.auth.local.dto.request.LocalRegisterRequest;

public interface LocalRegisterService {

	void register(LocalRegisterRequest request);
}
