package com.flowerplatform.rapp_mini_server.remote_object;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class RemoteObjectWebSocketServlet extends WebSocketServlet {

	private static final long serialVersionUID = 1L;

	private RemoteObjectHub hub;
	
	public RemoteObjectWebSocketServlet(RemoteObjectHub hub) {
		this.hub = hub;
	}

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.getPolicy().setIdleTimeout(10 * 60 * 1000); // 10 minutes
		factory.setCreator(new WebSocketCreator() {
			@Override
			public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
				return new RemoteObjectWebSocket(hub);
			}
		});
//		factory.register(RemoteObjectWebSocket.class);
	}

}
