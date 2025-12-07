package com.recorday.recorday.auth.service;

import com.recorday.recorday.auth.entity.CustomUserPrincipal;

public interface UserPrincipalLoader {
	CustomUserPrincipal loadUserByPublicId(String publicId);
}
