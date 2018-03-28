package ca.bcgov.didauth.proxy.verifying;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bcgov.didauth.proxy.util.HttpUtil;
import ca.bcgov.didauth.proxy.util.Signature;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import uniresolver.client.ClientUniResolver;
import uniresolver.did.DIDDocument;
import uniresolver.result.ResolutionResult;

public class VerifyingHttpFilters extends HttpFiltersAdapter implements HttpFilters {

	private static Logger log = LoggerFactory.getLogger(VerifyingHttpFilters.class);

	private final String resolveUri;
	private final String targetHost;
	private final String[] whitelist;

	public VerifyingHttpFilters(HttpRequest originalRequest, ChannelHandlerContext ctx, String resolveUri, String targetHost, String[] whitelist) {

		super(originalRequest, ctx);

		this.resolveUri = resolveUri;
		this.targetHost = targetHost;
		this.whitelist = whitelist;
	}

	public VerifyingHttpFilters(HttpRequest originalRequest, String resolveUri, String targetHost, String[] whitelist) {

		super(originalRequest);

		this.resolveUri = resolveUri;
		this.targetHost = targetHost;
		this.whitelist = whitelist;
	}

	@Override
	public HttpResponse clientToProxyRequest(HttpObject httpObject) {

		if (log.isDebugEnabled()) log.debug("clientToProxyRequest: " + httpObject);

		// parse request

		if (! (httpObject instanceof HttpRequest)) return super.proxyToServerRequest(httpObject);
		HttpRequest httpRequest = (HttpRequest) httpObject;
		HttpHeaders httpHeaders = httpRequest.headers();

		if (this.targetHost != null) {

			httpRequest.setUri("http://" + this.targetHost + httpRequest.getUri());
			httpHeaders.remove("Host");
			httpHeaders.add("Host", this.targetHost);
		}

		String method = httpRequest.getMethod().name();
		String uri = httpRequest.getUri();
		Map<String, String> headers = HttpUtil.headers(httpRequest.headers());

		try {

			// extract signature and signing DID

			Signature signature = HttpUtil.getSignature(headers);
			if (signature == null) throw new GeneralSecurityException("No signature found in headers.");

			String signingDid = signature.getKeyId();
			Verifier verifier = Verifier.verifier(signature.getAlgorithm());

			// resolve signing DID

			ClientUniResolver uniResolver = new ClientUniResolver();
			uniResolver.setResolveUri(this.resolveUri);

			ResolutionResult resolutionResult = uniResolver.resolve(signingDid);
			DIDDocument didDocument = resolutionResult != null ? resolutionResult.getDidDocument() : null;

			// find verifying key

			uniresolver.did.PublicKey didPublicKey = null;
			Object verifyingKey = null;

			List<uniresolver.did.PublicKey> didPublicKeys = new ArrayList<uniresolver.did.PublicKey> ();
			if (didDocument.getPublicKeys() != null) didPublicKeys.addAll(didDocument.getPublicKeys());
			if (didDocument.getAuthentications() != null) for (uniresolver.did.Authentication didAuthentication : didDocument.getAuthentications()) didPublicKeys.addAll(didAuthentication.getPublicKeys());
			if (log.isDebugEnabled()) log.debug("Found " + didPublicKeys.size() + " public keys in DID document.");

			for (int i=0; i<didPublicKeys.size(); i++) {

				didPublicKey = didPublicKeys.get(i);
				verifyingKey = verifier.verifyingKey(didPublicKey);
				if (verifyingKey != null) break;
			}

			if (verifyingKey == null) throw new GeneralSecurityException("Could not find verifying key of algorithm " + signature.getAlgorithm() + " for signing DID " + signingDid);

			// verify signature

			boolean verified = verifier.verify(method, uri, headers, signature, verifyingKey);
			if (! verified) throw new GeneralSecurityException("Signature cannot be verified with key " + verifyingKey);

			String verifiedDid = signingDid;

			// check against whitelist

			if (this.whitelist != null) {

				boolean whitelisted = Arrays.asList(this.whitelist).contains(verifiedDid);

				if (log.isDebugEnabled()) log.debug("Checking DID " + verifiedDid + " against whitelist: " + whitelisted);

				if (! whitelisted) {

					throw new GeneralSecurityException("DID " + verifiedDid + " not whitelisted.");
				}
			}

			// manipulate headers

			HttpUtil.setVerifiedDid(httpHeaders, verifiedDid);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Cannot verify: " + ex.getMessage(), ex);
			return HttpUtil.exceptionHttpResponse(httpRequest, "Cannot verify: " + ex.getMessage(), ex);
		}

		// done

		return super.proxyToServerRequest(httpObject);
	}

	/*
	 * Helper class
	 */

	public static class Source extends HttpFiltersSourceAdapter implements HttpFiltersSource {

		private final String resolveUri;
		private final String targetHost;
		private final String[] whitelist;

		public Source(String resolveUri, String targetHost, String[] whitelist) {

			this.resolveUri = resolveUri;
			this.targetHost = targetHost;
			this.whitelist = whitelist;
		}

		@Override
		public HttpFilters filterRequest(HttpRequest originalRequest) {

			return new VerifyingHttpFilters(originalRequest, this.resolveUri, this.targetHost, this.whitelist);
		}

		@Override
		public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {

			return new VerifyingHttpFilters(originalRequest, ctx, this.resolveUri, this.targetHost, this.whitelist);
		}
	}
}
