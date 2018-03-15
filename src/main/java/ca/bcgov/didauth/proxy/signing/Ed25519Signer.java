package ca.bcgov.didauth.proxy.signing;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.NaCl.Sodium;
import org.bitcoinj.core.Base58;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jnr.ffi.byref.LongLongByReference;

class Ed25519Signer extends Signer<byte[]> {

	private static Logger log = LoggerFactory.getLogger(Ed25519Signer.class);

	private static final String SIGNING_KEY_TYPE = "ed25519";
	private static final String ALGORITHM = "ed25519";

	static {

		NaCl.init();
	}

	Ed25519Signer() {

	}

	static boolean supports(String signingKeyType) {

		return SIGNING_KEY_TYPE.equals(signingKeyType);
	}

	@Override
	public String algorithm() {

		return ALGORITHM;
	}

	@Override
	public byte[] signingKey(String signingKeyString) throws GeneralSecurityException {

		return Base58.decode(signingKeyString);
	}

	@Override
	public byte[] sign(byte[] signingBytes, byte[] signingKey) throws GeneralSecurityException, IOException {

		// sign

		return signInternal(signingBytes, signingKey);
	}

	/*
	 * Helper methods
	 */

	private static byte[] signInternal(byte[] message, byte[] privateKey) throws GeneralSecurityException {

		if (privateKey.length != Sodium.CRYPTO_SIGN_ED25519_SECRETKEYBYTES) throw new GeneralSecurityException("Invalid private key length.");

		byte[] signatureValue = new byte[Sodium.CRYPTO_SIGN_ED25519_BYTES + message.length];
		Arrays.fill(signatureValue, 0, Sodium.CRYPTO_SIGN_ED25519_BYTES, (byte) 0);
		System.arraycopy(message, 0, signatureValue, Sodium.CRYPTO_SIGN_ED25519_BYTES, message.length);

		LongLongByReference bufferLen = new LongLongByReference();

		int ret = NaCl.sodium().crypto_sign_ed25519(signatureValue, bufferLen, message, message.length, privateKey);
		if (ret != 0) throw new GeneralSecurityException("Signing error: " + ret);

		signatureValue = Arrays.copyOfRange(signatureValue, 0, Sodium.CRYPTO_SIGN_ED25519_BYTES);

		return signatureValue;
	}
}