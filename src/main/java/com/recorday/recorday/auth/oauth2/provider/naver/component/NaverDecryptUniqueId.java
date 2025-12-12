package com.recorday.recorday.auth.oauth2.provider.naver.component;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.recorday.recorday.auth.oauth2.provider.naver.dto.NaverUnlinkRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverDecryptUniqueId {

	@Value("${spring.security.oauth2.client.registration.naver.client-secret}")
	private String clientSecret;

	private static final String ALGORITHM_AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
	private static final String ALGORITHM_AES = "AES";
	private static final int BLOCK_SIZE = 16;
	private static final String HMAC_ALGORITHM = "HmacSHA256";

	public String handleUnlinkNotification(NaverUnlinkRequest request) throws Exception {

		if (!verifySignature(request)) {
			throw new SecurityException("Invalid signature for Naver unlink request.");
		}

		final byte[] key = generateAesKey(clientSecret);

		return doDecryptUniqueId(request.encryptUniqueId(), key);
	}

	private boolean verifySignature(NaverUnlinkRequest request) throws NoSuchAlgorithmException, InvalidKeyException {
		String data = request.clientId() + request.encryptUniqueId() + request.timestamp();

		Mac mac = Mac.getInstance(HMAC_ALGORITHM);
		SecretKeySpec secretKey = new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
		mac.init(secretKey);
		byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

		String calculatedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);

		return calculatedSignature.equals(request.signature());
	}

	private byte[] generateAesKey(final String clientSecret) throws NoSuchAlgorithmException {
		final MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(clientSecret.getBytes(StandardCharsets.UTF_8));

		return md.digest();
	}

	private String doDecryptUniqueId(final String encrypted, final byte[] aesKey) throws Exception {

		final byte[] encryptedWithIV = Base64.getUrlDecoder().decode(encrypted);

		final byte[] iv = Arrays.copyOfRange(encryptedWithIV, 0, BLOCK_SIZE);
		final byte[] encryptedUniqueId = Arrays.copyOfRange(encryptedWithIV, BLOCK_SIZE, encryptedWithIV.length);

		final SecretKeySpec skeySpec = new SecretKeySpec(aesKey, ALGORITHM_AES);
		final Cipher cipher = Cipher.getInstance(ALGORITHM_AES_CBC_PKCS5);
		final IvParameterSpec ivspec = new IvParameterSpec(iv);

		cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

		final byte[] decrypted = cipher.doFinal(encryptedUniqueId);

		return new String(decrypted, StandardCharsets.UTF_8);
	}
}
