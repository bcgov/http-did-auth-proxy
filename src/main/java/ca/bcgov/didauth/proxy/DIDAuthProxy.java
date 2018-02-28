package ca.bcgov.didauth.proxy;

import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bcgov.didauth.proxy.chained.MyChainedProxyManager;
import ca.bcgov.didauth.proxy.signing.SigningHttpFilters;
import ca.bcgov.didauth.proxy.verifying.VerifyingHttpFilters;

public class DIDAuthProxy {

	private static Logger log = LoggerFactory.getLogger(DIDAuthProxy.class);

	public static void main(String[] args) throws Exception {

		HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer.bootstrap();

		// configure server

		if (log.isDebugEnabled()) log.debug("Signing: " + Configuration.signing());
		if (Configuration.signing() && Configuration.signingDid() != null && Configuration.signingKey() != null && Configuration.signingKeyType() != null) {

			if (log.isDebugEnabled()) log.debug("Signing DID: " + Configuration.signingDid());
			if (log.isDebugEnabled()) log.debug("Signing key: " + Configuration.signingKey());
			if (log.isDebugEnabled()) log.debug("Signing key type: " + Configuration.signingKeyType());
			bootstrap = bootstrap.withFiltersSource(new SigningHttpFilters.Source(Configuration.signingDid(), Configuration.signingKey(), Configuration.signingKeyType()));
		} else if (! Configuration.signing() && Configuration.resolverUri() != null) {

			if (log.isDebugEnabled()) log.debug("Resolver URI: " + Configuration.resolverUri());
			if (log.isDebugEnabled()) log.debug("Target host: " + Configuration.targetHost());
			bootstrap = bootstrap.withFiltersSource(new VerifyingHttpFilters.Source(Configuration.resolverUri(), Configuration.targetHost()));
		} else {

			throw new IllegalArgumentException("Missing configuration settings EITHER: 'signing=1' and 'signingDid' and 'signingKey' and 'signingKeyType', OR: 'signing=0' and 'resolverUri'");
		}

		if (Configuration.port() != -1) {

			if (log.isDebugEnabled()) log.debug("Port: " + Configuration.port());
			bootstrap.withPort(Configuration.port());
		} else {

			throw new IllegalArgumentException("Missing configuration setting: 'port'");
		}

		if (Configuration.upstreamHost() != null && Configuration.upstreamPort() != -1) {

			if (log.isDebugEnabled()) log.debug("Upstream host: " + Configuration.upstreamHost());
			if (log.isDebugEnabled()) log.debug("Upstream port: " + Configuration.upstreamPort());
			bootstrap = bootstrap.withChainProxyManager(new MyChainedProxyManager(Configuration.upstreamHost(), Configuration.upstreamPort()));
		}

		bootstrap.withAllowLocalOnly(false);

		if (log.isDebugEnabled()) log.debug("Starting DID Auth proxy:" + bootstrap);

		// start server

		bootstrap.start();
	}
}
