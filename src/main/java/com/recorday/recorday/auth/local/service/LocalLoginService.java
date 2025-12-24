package com.recorday.recorday.auth.local.service;

import com.recorday.recorday.auth.jwt.dto.TokenResponse;
import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;

public interface LocalLoginService {

	TokenResponse login(LocalLoginRequest request);

}
