package ca.bcgov.didauth.proxy.signing;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.NaCl.Sodium;
import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.Base58;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bcgov.didauth.proxy.util.Signature;
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
	public byte[] signingKey(String signingKeyString) throws GeneralSecurityException {

		return Base58.decode(signingKeyString);
	}

	@Override
	public Signature sign(byte[] signingBytes, String signingDid, List<String> signedHeaderNames, byte[] signingKey) throws GeneralSecurityException, IOException {

		// sign

		byte[] signatureBytes = sign(signingBytes, signingKey);
		String signatureString = Base64.encodeBase64String(signatureBytes);
		if (log.isDebugEnabled()) log.debug("Signature string: " + signatureString);

		Signature signature = new Signature(signingDid, ALGORITHM, signatureString, signedHeaderNames);
		if (log.isDebugEnabled()) log.debug("Signature: " + signature);

		return signature;
	}

	/*
	 * Helper methods
	 */

	private static byte[] sign(byte[] message, byte[] privateKey) throws GeneralSecurityException {

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