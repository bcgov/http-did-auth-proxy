package ca.bcgov.didauth.proxy.signing;

import java.security.GeneralSecurityException;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;

class Secp256k1Signer extends Signer<ECKey> {

	private static final String SIGNING_KEY_TYPE = "secp256k1";
	private static final String ALGORITHM = "secp256k1";

	Secp256k1Signer() {

	}

	static boolean supports(String signingKeyType) {

		return SIGNING_KEY_TYPE.equals(signingKeyType);
	}

	@Override
	public String algorithm() {

		return ALGORITHM;
	}

	@Override
	public ECKey signingKey(String signingKeyString) throws GeneralSecurityException {

		DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, signingKeyString);

		return dpk.getKey();
	}

	@Override
	public byte[] sign(byte[] signingBytes, ECKey signingKey) {

		return signingKey.sign(Sha256Hash.wrap(signingBytes)).encodeToDER();
	}
}