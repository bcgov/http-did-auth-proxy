package ca.bcgov.didauth.proxy.verifying;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.NaCl.Sodium;
import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.Base58;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import jnr.ffi.byref.LongLongByReference;

class Ed25519Verifier extends Verifier<byte[]> {

	private static Logger log = LoggerFactory.getLogger(Ed25519Verifier.class);

	private static final String DID_PUBLIC_KEY_TYPE = "Ed25519VerificationKey2018";
	private static final String ALGORITHM = "ed25519";

	static {

		NaCl.init();
	}

	Ed25519Verifier() {

	}

	static boolean supports(String algorithm) {

		return ALGORITHM.equals(algorithm);
	}

	@Override
	public byte[] verifyingKey(uniresolver.did.PublicKey didPublicKey) throws GeneralSecurityException {

		if (! didPublicKey.isType(DID_PUBLIC_KEY_TYPE)) {

			if (log.isDebugEnabled()) log.debug("Public key " + didPublicKey.getId() + " is not of type " + DID_PUBLIC_KEY_TYPE);
			return null;
		}

		String verificationKeyString;

		verificationKeyString = didPublicKey.getPublicKeyBase64();
		if (verificationKeyString != null) return Base64.decodeBase64(verificationKeyString);
		verificationKeyString = didPublicKey.getPublicKeyBase58();
		if (verificationKeyString != null) return Base58.decode(verificationKeyString);
		verificationKeyString = didPublicKey.getPublicKeyHex();
		if (verificationKeyString != null) return Hex.decode(verificationKeyString);

		if (log.isDebugEnabled()) log.debug("Public key " + didPublicKey.getId() + " has no data.");
		return null;
	}

	@Override
	public boolean verify(byte[] signingBytes, byte[] signatureBytes, byte[] verifyingKey) throws GeneralSecurityException, IOException {

		boolean verified = verifyInternal(signingBytes, signatureBytes, verifyingKey);

		return verified;
	}

	/*
	 * Helper methods
	 */

	public boolean verifyInternal(byte[] signingBytes, byte[] signatureBytes, byte[] publicKey) throws GeneralSecurityException {

		if (signatureBytes.length != Sodium.CRYPTO_SIGN_ED25519_BYTES) throw new GeneralSecurityException("Invalid signature length.");
		if (publicKey.length != Sodium.CRYPTO_SIGN_ED25519_PUBLICKEYBYTES) throw new GeneralSecurityException("Invalid public key length.");

		byte[] sigAndMsg = new byte[signatureBytes.length + signingBytes.length];
		System.arraycopy(signatureBytes, 0, sigAndMsg, 0, signatureBytes.length);
		System.arraycopy(signingBytes, 0, sigAndMsg, signatureBytes.length, signingBytes.length);

		byte[] buffer = new byte[sigAndMsg.length];
		LongLongByReference bufferLen = new LongLongByReference();

		int ret = NaCl.sodium().crypto_sign_ed25519_open(buffer, bufferLen, sigAndMsg, sigAndMsg.length, publicKey);
		if (ret != 0) return false;

		buffer = Arrays.copyOf(buffer, buffer.length - Sodium.CRYPTO_SIGN_ED25519_BYTES);

		return Arrays.equals(signingBytes, buffer);
	}
}