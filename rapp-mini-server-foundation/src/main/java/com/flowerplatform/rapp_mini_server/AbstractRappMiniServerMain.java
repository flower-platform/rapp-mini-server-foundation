package com.flowerplatform.rapp_mini_server;

import com.crispico.flower_platform.remote_object.RemoteObjectHubServlet;
import com.crispico.flower_platform.remote_object.RemoteObjectServlet;
import com.crispico.flower_platform.remote_object.WebSocketServerEndpoint;
import com.stijndewitt.undertow.cors.Filter;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

/**
 * @author Cristian Spiescu
 */
public abstract class AbstractRappMiniServerMain {

	protected int port = 9000;

	protected boolean threadJoin = false;

	protected void run() {
		try {
			@SuppressWarnings("unchecked")
			DeploymentInfo deploymentInfo = Servlets.deployment()
			        .setClassLoader(getClass().getClassLoader())
			        .setContextPath("/")
			        .setDeploymentName(getClass().getName());
			
			configureDeploymentInfo(deploymentInfo);
			
			DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
			manager.deploy();
			PathHandler path = Handlers.path().addPrefixPath("/", manager.start());
			Filter corsHandler = new com.stijndewitt.undertow.cors.Filter(path);
			corsHandler.setUrlPattern("(.*)");
			corsHandler.setPolicyClass("com.stijndewitt.undertow.cors.AllowAll");
			
			Undertow server = Undertow.builder()
			        .addHttpListener(port, "0.0.0.0")
			        .setHandler(corsHandler)
			        .build();
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void configureDeploymentInfo(DeploymentInfo deploymentInfo) {
		deploymentInfo
			.addServlets(Servlets.servlet(RemoteObjectServlet.class.getName(), RemoteObjectServlet.class).addMapping("/remoteObject/*").addInitParam("securityToken", "12345678"))
			.addServlets(Servlets.servlet(RemoteObjectHubServlet.class.getName(), RemoteObjectHubServlet.class).addMapping("/hub/*").addInitParam("securityToken", "12345678"))
			.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, new WebSocketDeploymentInfo().addEndpoint(WebSocketServerEndpoint.class));
	}
}
