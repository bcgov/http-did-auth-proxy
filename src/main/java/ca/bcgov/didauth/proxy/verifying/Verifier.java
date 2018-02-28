package ca.bcgov.didauth.proxy.verifying;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Base64;
import org.tomitribe.auth.signatures.Signatures;

import ca.bcgov.didauth.proxy.util.HttpUtil;
import ca.bcgov.didauth.proxy.util.Signature;

abstract class Verifier<KEY> {

	private static Logger log = LoggerFactory.getLogger(Verifier.class);

	public static Verifier<?> verifier(String algorithm) {

		if (RsaVerifier.supports(algorithm)) return new RsaVerifier();
		if (Ed25519Verifier.supports(algorithm)) return new Ed25519Verifier();
		if (Secp256k1Verifier.supports(algorithm)) return new Secp256k1Verifier();

		throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
	}

	public abstract KEY verifyingKey(uniresolver.did.PublicKey didPublicKey) throws GeneralSecurityException;
	public abstract boolean verify(byte[] signingBytes, byte[] signatureBytes, KEY verifyingKey) throws GeneralSecurityException, IOException;

	public boolean verify(String method, String uri, Map<String, String> headers, Signature signature, KEY verifyingKey) throws GeneralSecurityException, IOException {

		if (log.isDebugEnabled()) log.debug("Method: " + method);
		if (log.isDebugEnabled()) log.debug("URI: " + uri);
		if (log.isDebugEnabled()) log.debug("Headers: " + headers);

		List<String> signedHeaderNames = HttpUtil.signedHeaderNames(headers);
		if (log.isDebugEnabled()) log.debug("Signed header names: " + signedHeaderNames);

		String signingString = Signatures.createSigningString(signedHeaderNames, method, uri, headers);
		byte[] signingBytes = signingString.getBytes(StandardCharsets.UTF_8);
		if (log.isDebugEnabled()) log.debug("Signing string: " + signingString);

		String signatureString = signature.getSignature();
		byte[] signatureBytes = Base64.decode(signatureString);
		if (log.isDebugEnabled()) log.debug("Signature string: " + signatureString);

		return this.verify(signingBytes, signatureBytes, verifyingKey);
	}
}