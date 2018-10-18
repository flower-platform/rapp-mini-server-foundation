package com.flowerplatform.rapp_mini_server.remote_object.obsolete;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.crispico.flower_platform.remote_object.RemoteObjectHub;

public class RemoteObjectWebSocketServlet extends WebSocketServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.getPolicy().setIdleTimeout(10 * 60 * 1000); // 10 minutes
		factory.setCreator(new WebSocketCreator() {
			@Override
			public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
				return new RemoteObjectWebSocket(RemoteObjectHub.getInstance());
			}
		});
//		factory.register(RemoteObjectWebSocket.class);
	}

}
