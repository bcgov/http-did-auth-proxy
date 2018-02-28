package ca.bcgov.didauth.proxy;

public class Configuration {

	public static final int DEFAULT_PORT = 8080;

	static boolean signing() {

		String signing = System.getenv("signing");
		if (signing == null) return false;

		return "1".equals(signing) || "true".equals(signing);
	}

	static int port() {

		String port = System.getenv("port");
		if (port == null) return DEFAULT_PORT;

		return Integer.parseInt(port);
	}

	static String upstreamHost() {

		String upstreamHost = System.getenv("upstreamHost");
		if (upstreamHost == null) return null;

		return upstreamHost;
	}

	public static int upstreamPort() {

		String upstreamPort = System.getenv("upstreamPort");
		if (upstreamPort == null) return -1;

		return Integer.parseInt(upstreamPort);
	}

	static String signingDid() {

		String signingDid = System.getenv("signingDid");
		if (signingDid == null) return null;

		return signingDid;
	}

	static String signingKey() {

		String signingKey = System.getenv("signingKey");
		if (signingKey == null) return null;

		return signingKey;
	}

	static String signingKeyType() {

		String signingKeyType = System.getenv("signingKeyType");
		if (signingKeyType == null) return null;

		return signingKeyType;
	}

	static String resolverUri() {

		String resolverUri = System.getenv("resolverUri");
		if (resolverUri == null) return null;

		return resolverUri;
	}

	static String targetHost() {

		String targetHost = System.getenv("targetHost");
		if (targetHost == null) return null;

		return targetHost;
	}
}

