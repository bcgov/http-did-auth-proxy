package ca.bcgov.didauth.proxy.verifying;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

class Secp256k1Verifier extends Verifier<ECKey> {

	private static Logger log = LoggerFactory.getLogger(Secp256k1Verifier.class);

	private static final String DID_PUBLIC_KEY_TYPE = "Secp256k1VerificationKey2018";
	private static final String ALGORITHM = "secp256k1";

	Secp256k1Verifier() {

	}

	static boolean supports(String algorithm) {

		return ALGORITHM.equals(algorithm);
	}

	@Override
	public ECKey verifyingKey(uniresolver.did.PublicKey didPublicKey) throws GeneralSecurityException {

		if (! didPublicKey.isType(DID_PUBLIC_KEY_TYPE)) {

			if (log.isDebugEnabled()) log.debug("Public key " + didPublicKey.getId() + " is not of type " + DID_PUBLIC_KEY_TYPE);
			return null;
		}

		String verificationKeyString;

		verificationKeyString = didPublicKey.getPublicKeyBase64();
		if (verificationKeyString != null) return ECKey.fromPublicOnly(Base64.decodeBase64(verificationKeyString));
		verificationKeyString = didPublicKey.getPublicKeyBase58();
		if (verificationKeyString != null) return ECKey.fromPublicOnly(Base58.decode(verificationKeyString));
		verificationKeyString = didPublicKey.getPublicKeyHex();
		if (verificationKeyString != null) return ECKey.fromPublicOnly(Hex.decode(verificationKeyString));

		if (log.isDebugEnabled()) log.debug("Public key " + didPublicKey.getId() + " has no data.");
		return null;
	}

	@Override
	public boolean verify(byte[] signingBytes, byte[] signatureBytes, ECKey verifyingKey) throws GeneralSecurityException, IOException {

		return false;
	}
}