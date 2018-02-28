package ca.bcgov.didauth.proxy.chained;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Queue;

import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpRequest;

public class MyChainedProxyManager implements ChainedProxyManager {

	private static Logger log = LoggerFactory.getLogger(MyChainedProxyManager.class);

	private String upstreamHost;
	private int upstreamPort;

	public MyChainedProxyManager(String upstreamHost, int upstreamPort) {

		this.upstreamHost = upstreamHost;
		this.upstreamPort = upstreamPort;
	}

	@Override
	public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {

		ChainedProxy chainedProxy = new ProvingChainedProxy();
		if (log.isDebugEnabled()) log.debug("Adding upstream proxy: " + chainedProxy.getChainedProxyAddress());

		chainedProxies.add(chainedProxy);
	}

	/*
	 * Helper class
	 */

	private class ProvingChainedProxy extends ChainedProxyAdapter {

		@Override
		public InetSocketAddress getChainedProxyAddress() {

			try {

				return new InetSocketAddress(InetAddress.getByName(MyChainedProxyManager.this.upstreamHost), MyChainedProxyManager.this.upstreamPort);
			} catch (UnknownHostException ex) {

				throw new RuntimeException("Unable to resolve " + MyChainedProxyManager.this.upstreamHost + ": " + ex.getMessage(), ex);
			}
		}	
	}
}
