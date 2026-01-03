package com.recorday.recorday.auth.local.service;

import com.recorday.recorday.auth.local.dto.request.LocalLoginRequest;
import com.recorday.recorday.auth.local.dto.response.LoginResult;

public interface LocalLoginService {

	LoginResult login(LocalLoginRequest request);

}
