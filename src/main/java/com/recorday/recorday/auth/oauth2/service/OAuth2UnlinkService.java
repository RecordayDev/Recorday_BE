package com.recorday.recorday.auth.oauth2.service;

import com.recorday.recorday.auth.oauth2.enums.Provider;
import com.recorday.recorday.user.entity.User;

public interface OAuth2UnlinkService {

	boolean supports(Provider provider);

	void unlink(User user);
}
