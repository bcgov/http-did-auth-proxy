package ca.bcgov.didauth.proxy.signing;

import java.security.GeneralSecurityException;
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

public class SigningHttpFilters extends HttpFiltersAdapter implements HttpFilters {

	private static Logger log = LoggerFactory.getLogger(SigningHttpFilters.class);

	private final String signingDid;
	private final String signingKey;
	private final String signingKeyType;

	public SigningHttpFilters(HttpRequest originalRequest, ChannelHandlerContext ctx, String signingDid, String signingKey, String signingKeyType) {

		super(originalRequest, ctx);

		this.signingDid = signingDid;
		this.signingKey = signingKey;
		this.signingKeyType = signingKeyType;
	}

	public SigningHttpFilters(HttpRequest originalRequest, String signingDid, String signingKey, String signingKeyType) {

		super(originalRequest);

		this.signingDid = signingDid;
		this.signingKey = signingKey;
		this.signingKeyType = signingKeyType;
	}

	private Signer<?> signer = null;
	private Object signingKeyObject = null;

	private Signer<?> signer() throws GeneralSecurityException {

		if (this.signer == null) this.signer = Signer.signer(this.signingKeyType);

		return this.signer;
	}

	private Object signingKey() throws GeneralSecurityException {
		
		if (this.signingKeyObject == null) this.signingKeyObject = this.signer().signingKey(this.signingKey);
		
		return this.signingKeyObject;
	}
	
	@Override
	public HttpResponse clientToProxyRequest(HttpObject httpObject) {

		if (log.isDebugEnabled()) log.debug("clientToProxyRequest: " + httpObject);

		// parse request
		
		if (! (httpObject instanceof HttpRequest)) return super.proxyToServerRequest(httpObject);
		HttpRequest httpRequest = (HttpRequest) httpObject;
		HttpHeaders httpHeaders = httpRequest.headers();

		String method = httpRequest.getMethod().name();
		String uri = httpRequest.getUri();
		Map<String, String> headers = HttpUtil.headers(httpRequest.headers());

		try {

			// calculate signature

			Signer signer = this.signer();
			Signature signature = signer.sign(method, uri, headers, this.signingDid, this.signingKey());

			// manipulate headers

			HttpUtil.setSignature(httpHeaders, signature);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Cannot sign: " + ex.getMessage(), ex);
			return HttpUtil.exceptionHttpResponse(httpRequest, "Cannot sign: " + ex.getMessage(), ex);
		}

		// done

		return super.proxyToServerRequest(httpObject);
	}

	/*
	 * Helper class
	 */

	public static class Source extends HttpFiltersSourceAdapter implements HttpFiltersSource {

		private final String signingDid;
		private final String signingKey;
		private final String signingKeyType;

		public Source(String signingDid, String signingKey, String signingKeyType) {

			this.signingDid = signingDid;
			this.signingKey = signingKey;
			this.signingKeyType = signingKeyType;
		}

		@Override
		public HttpFilters filterRequest(HttpRequest originalRequest) {

			return new SigningHttpFilters(originalRequest, this.signingDid, this.signingKey, this.signingKeyType);
		}

		@Override
		public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {

			return new SigningHttpFilters(originalRequest, ctx, this.signingDid, this.signingKey, this.signingKeyType);
		}
	}
}
