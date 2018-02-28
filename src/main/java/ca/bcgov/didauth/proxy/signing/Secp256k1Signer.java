package ca.bcgov.didauth.proxy.signing;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.bitcoinj.core.Base58;

import ca.bcgov.didauth.proxy.util.Signature;

class Secp256k1Signer extends Signer<byte[]> {

	private static final String SIGNING_KEY_TYPE = "secp256k1";
	private static final String ALGORITHM = "secp256k1";

	Secp256k1Signer() {

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

		return null;
	}
}