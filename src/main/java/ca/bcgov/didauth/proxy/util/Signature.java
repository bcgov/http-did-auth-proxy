package ca.bcgov.didauth.proxy.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tomitribe.auth.signatures.AuthenticationException;
import org.tomitribe.auth.signatures.Join;
import org.tomitribe.auth.signatures.MissingAlgorithmException;
import org.tomitribe.auth.signatures.MissingKeyIdException;
import org.tomitribe.auth.signatures.MissingSignatureException;
import org.tomitribe.auth.signatures.UnparsableSignatureException;

public class Signature {

	private final String keyId;
	private final String algorithm;
	private final String signature;

	private final List<String> headers;

	public Signature(final String keyId, final String algorithm, final String signature, final String... headers) {
		this(keyId, algorithm, signature, Arrays.asList(headers));
	}

	public Signature(final String keyId, final String algorithm, final String signature, final List<String> headers) {

		if (keyId == null || keyId.trim().isEmpty()) throw new IllegalArgumentException("keyId is required.");
		if (algorithm == null) throw new IllegalArgumentException("algorithm is required.");

		this.keyId = keyId;
		this.algorithm = algorithm;
		this.signature = signature;

		if (headers.size() == 0) {

			final List<String> list = Arrays.asList("date");
			this.headers = Collections.unmodifiableList(list);
		} else {

			this.headers = Collections.unmodifiableList(lowercase(headers));
		}
	}

	private List<String> lowercase(List<String> headers) {

		final List<String> list = new ArrayList<String>(headers.size());
		for (String header : headers) list.add(header.toLowerCase());

		return list;
	}

	public String getKeyId() {

		return this.keyId;
	}

	public String getAlgorithm() {

		return this.algorithm;
	}

	public String getSignature() {

		return this.signature;
	}

	public List<String> getHeaders() {

		return this.headers;
	}

	public static Signature fromString(String authorization) {

		try {

			authorization = normalize(authorization);

			final String[] split = authorization.split(",");
			final Map<String, String> map = new HashMap<String, String>();

			for (String s : split) {

				s = s.trim();
				final int i = s.indexOf("=\"");

				final String key = s.substring(0, i).toLowerCase();
				final String value = s.substring(i + 2, s.length() - 1);

				map.put(key, value);
			}

			final List<String> headers = new ArrayList<String>();
			final String headerString = map.get("headers");
			if (headerString != null) Collections.addAll(headers, headerString.toLowerCase().split(" +"));

			final String keyid = map.get("keyid");
			if (keyid == null) throw new MissingKeyIdException();

			final String algorithm = map.get("algorithm");
			if (algorithm == null) throw new MissingAlgorithmException();

			final String signature = map.get("signature");
			if (signature == null) throw new MissingSignatureException();

			return new Signature(keyid, algorithm, signature, headers);
		} catch (AuthenticationException ex) {

			throw ex;
		} catch (Throwable ex) {

			throw new UnparsableSignatureException(authorization, ex);
		}
	}

	private static String normalize(String authorization) {

		final String start = "signature ";
		final String prefix = authorization.substring(0, start.length()).toLowerCase();

		if (prefix.equals(start)) authorization = authorization.substring(start.length());

		return authorization.trim();
	}

	@Override
	public String toString() {

		return "Signature " +
				"keyId=\"" + this.keyId + '\"' +
				",algorithm=\"" + this.algorithm + '\"' +
				",headers=\"" + Join.join(" ", this.headers) + '\"' +
				",signature=\"" + this.signature + '\"';
	}
}
