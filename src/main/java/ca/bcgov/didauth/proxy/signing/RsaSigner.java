package ca.bcgov.didauth.proxy.signing;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

class RsaSigner extends Signer<PrivateKey> {

	private static final String SIGNING_KEY_TYPE = "rsa";
	private static final String ALGORITHM = "rsa-sha256";
	private static final String JVM_ALGORITHM = "SHA256withRSA";

	private static KeyFactory keyFactory;

	static {

		try {

			keyFactory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException ex) {

			throw new ExceptionInInitializerError(ex);
		}
	}

	RsaSigner() {

	}

	static boolean supports(String signingKeyType) {

		return SIGNING_KEY_TYPE.equals(signingKeyType);
	}

	@Override
	public String algorithm() {

		return ALGORITHM;
	}

	@Override
	public PrivateKey signingKey(String signingKeyString) throws GeneralSecurityException {

		signingKeyString = signingKeyString.replace("-----BEGIN PRIVATE KEY-----", "");
		signingKeyString = signingKeyString.replace("-----END PRIVATE KEY-----", "");
		signingKeyString = signingKeyString.replace("\\r", "");
		signingKeyString = signingKeyString.replace("\\n", "");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(signingKeyString));

		return keyFactory.generatePrivate(keySpec);
	}

	@Override
	public byte[] sign(byte[] signingBytes, PrivateKey signingKey) throws GeneralSecurityException, IOException {

		java.security.Signature instance = java.security.Signature.getInstance(JVM_ALGORITHM);

		instance.initSign(signingKey);
		instance.update(signingBytes);
		byte[] signatureBytes = instance.sign();

		return signatureBytes;
	}
}