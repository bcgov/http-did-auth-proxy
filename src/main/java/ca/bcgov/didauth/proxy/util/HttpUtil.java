package ca.bcgov.didauth.proxy.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpUtil {

	private static Logger log = LoggerFactory.getLogger(HttpUtil.class);

	public static final String HEADER_CONNECTION = "Connection";
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_VERIFIED_DID = "Verified-DID";

	public static final String[] REMOVE_HEADERS = new String[] { "Authorization", "Connection", "Proxy-Connection", "Via", "Host", "X-Forwarded-Host", "X-Forwarded-Proto", "Forwarded", "X-Forwarded-For", "X-Forwarded-Port" };

	public static HttpResponse exceptionHttpResponse(HttpRequest httpRequest, String message, Exception ex) {

		StringWriter buffer = new StringWriter();
		PrintWriter writer = new PrintWriter(buffer);
		writer.println(message);
		ex.printStackTrace(writer);

		ByteBuf content = Unpooled.copiedBuffer(buffer.getBuffer().toString().toCharArray(), StandardCharsets.UTF_8);

		DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(httpRequest.getProtocolVersion(), HttpResponseStatus.INTERNAL_SERVER_ERROR, content);
		httpResponse.headers().add(HEADER_CONNECTION, "close");

		return httpResponse;
	}

	public static Map<String, String> headers(HttpHeaders httpHeaders) {

		Map<String, String> headers = new HashMap<String, String> ();

		for (String name : httpHeaders.names()) {

			String value = httpHeaders.get(name);

			if (log.isDebugEnabled()) log.debug("Header: " + name + " -> " + value);
			headers.put(name, value);
		}

		return headers;
	}

	public static List<String> signedHeaderNames(Map<String, String> headers) {

		List<String> signedHeaderNames = new ArrayList<String> ();
		//signedHeaderNames.add("(request-target)");
		for (String header : headers.keySet()) signedHeaderNames.add(header.toLowerCase());
		for (String removeHeader : REMOVE_HEADERS) signedHeaderNames.remove(removeHeader.toLowerCase());

		return signedHeaderNames;
	}

	public static void setSignature(HttpHeaders httpHeaders, Signature signature) {

		String value = signature.toString();

		if (log.isDebugEnabled()) log.debug("Adding " + HEADER_AUTHORIZATION + " header: " + value);

		httpHeaders.add(HEADER_AUTHORIZATION, value);
	}

	public static Signature getSignature(Map<String, String> headers) {

		String value = headers.get(HEADER_AUTHORIZATION);

		if (log.isDebugEnabled()) log.debug("Got " + HEADER_AUTHORIZATION + " header: " + value);

		Signature signature = Signature.fromString(value);

		return signature;
	}

	public static void setVerifiedDid(HttpHeaders httpHeaders, String verifiedDid) {

		String value = verifiedDid;

		if (log.isDebugEnabled()) log.debug("Adding " + HEADER_VERIFIED_DID + " header: " + value);

		httpHeaders.add(HEADER_VERIFIED_DID, value);
	}

	public static String getVerifiedDidHeader(Map<String, String> headers) {

		String value = headers.get(HEADER_VERIFIED_DID);

		if (log.isDebugEnabled()) log.debug("Got " + HEADER_VERIFIED_DID + " header: " + value);

		String verifiedDid = value;

		return verifiedDid;
	}
}
