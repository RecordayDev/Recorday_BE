package com.recorday.recorday.auth.service;

import com.recorday.recorday.auth.jwt.dto.TokenResponse;

public interface AuthorizationCodeTokenService {
	TokenResponse issueTokens(String code);
}
