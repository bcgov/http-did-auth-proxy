package ca.bcgov.didauth.proxy.verifying;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RsaVerifier extends Verifier<PublicKey> {

	private static Logger log = LoggerFactory.getLogger(RsaVerifier.class);

	private static final String DID_PUBLIC_KEY_TYPE = "RsaVerificationKey2018";
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

	RsaVerifier() {

	}

	static boolean supports(String algorithm) {

		return ALGORITHM.equals(algorithm);
	}

	@Override
	public PublicKey verifyingKey(uniresolver.did.PublicKey didPublicKey) throws GeneralSecurityException {

		if (! didPublicKey.isType(DID_PUBLIC_KEY_TYPE)) {

			if (log.isDebugEnabled()) log.debug("Public key " + didPublicKey.getId() + " is not of type " + DID_PUBLIC_KEY_TYPE);
			return null;
		}

		String verifyingKeyString = didPublicKey.getPublicKeyPem();
		if (verifyingKeyString == null) {

			if (log.isDebugEnabled()) log.debug("Public key " + didPublicKey.getId() + " has no data.");
			return null;
		}

		verifyingKeyString = verifyingKeyString.replace("-----BEGIN PUBLIC KEY-----", "");
		verifyingKeyString = verifyingKeyString.replace("-----END PUBLIC KEY-----", "");
		verifyingKeyString = verifyingKeyString.replace("\\r", "");
		verifyingKeyString = verifyingKeyString.replace("\\n", "");

		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(verifyingKeyString));

		return keyFactory.generatePublic(keySpec);

	}

	@Override
	public boolean verify(byte[] signingBytes, byte[] signatureBytes, PublicKey verifyingKey) throws GeneralSecurityException, IOException {

		java.security.Signature instance = java.security.Signature.getInstance(JVM_ALGORITHM);

		instance.initVerify(verifyingKey);
		instance.update(signingBytes);
		boolean verified = instance.verify(signatureBytes);
		if (log.isDebugEnabled()) log.debug("Verified: " + verified);

		return verified;
	}
}