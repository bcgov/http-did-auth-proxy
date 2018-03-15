package ca.bcgov.didauth.proxy.signing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomitribe.auth.signatures.Signatures;

import ca.bcgov.didauth.proxy.util.HttpUtil;
import ca.bcgov.didauth.proxy.util.Signature;

abstract class Signer<KEY> {

	private static Logger log = LoggerFactory.getLogger(Signer.class);

	public static Signer<?> signer(String signingKeyType) {

		if (RsaSigner.supports(signingKeyType)) return new RsaSigner();
		if (Ed25519Signer.supports(signingKeyType)) return new Ed25519Signer();
		if (Secp256k1Signer.supports(signingKeyType)) return new Secp256k1Signer();

		throw new IllegalArgumentException("Unknown signing key type: " + signingKeyType);
	}

	public abstract String algorithm();
	public abstract KEY signingKey(String signingKeyString) throws GeneralSecurityException;
	public abstract byte[] sign(byte[] signingBytes, KEY signingKey) throws GeneralSecurityException, IOException;

	public Signature sign(String method, String uri, Map<String, String> headers, String signingDid, KEY signingKey) throws GeneralSecurityException, IOException {

		if (log.isDebugEnabled()) log.debug("Method: " + method);
		if (log.isDebugEnabled()) log.debug("URI: " + uri);
		if (log.isDebugEnabled()) log.debug("Headers: " + headers);

		List<String> signedHeaderNames = HttpUtil.signedHeaderNames(headers);
		if (log.isDebugEnabled()) log.debug("Signed header names: " + signedHeaderNames);

		String signingString = Signatures.createSigningString(signedHeaderNames, method, uri, headers);
		byte[] signingBytes = signingString.getBytes(StandardCharsets.UTF_8);
		if (log.isDebugEnabled()) log.debug("Signing string: " + signingString);

		byte[] signatureBytes = this.sign(signingBytes, signingKey);
		String signatureString = Base64.encodeBase64String(signatureBytes);
		if (log.isDebugEnabled()) log.debug("Signature string: " + signatureString);

		Signature signature = new Signature(signingDid, this.algorithm(), signatureString, signedHeaderNames);
		if (log.isDebugEnabled()) log.debug("Signature: " + signature);

		return signature;
	}
}