package com.recorday.recorday.auth.local.service;

import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.AuthTokenResponse;

public interface LocalLoginService {

	AuthTokenResponse login(LocalLoginRequest request);

}
