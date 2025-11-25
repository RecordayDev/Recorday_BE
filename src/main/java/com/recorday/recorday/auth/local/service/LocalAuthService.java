package com.recorday.recorday.auth.local.service;

import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.LocalLoginResponse;

public interface LocalAuthService {

	LocalLoginResponse login(LocalLoginRequest request);

}
