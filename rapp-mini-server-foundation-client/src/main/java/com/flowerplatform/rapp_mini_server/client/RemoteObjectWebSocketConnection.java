package com.flowerplatform.rapp_mini_server.client;

import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;
import com.flowerplatform.rapp_mini_server.shared.IRemoteObjectServiceInvoker;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsType;

@JsType(namespace="rapp_mini_server")
public class RemoteObjectWebSocketConnection {

	private WebSocket webSocket; 

	/**
	 * May be null (e.g. for GWT client)
	 */
	private IRemoteObjectServiceInvoker serviceInvoker;
	
	/**
	 * IP address or hostname of the hub.  
	 */
	private String remoteAddress;
	
	private String securityToken;
	
	/**
	 * Local rapp instance id
	 */
	private String localNodeId;

	private int localServerPort;

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public String getLocalNodeId() {
		return localNodeId;
	}

	public void setLocalNodeId(String localNodeId) {
		this.localNodeId = localNodeId;
	}

	public IRemoteObjectServiceInvoker getServiceInvoker() {
		return serviceInvoker;
	}

	public int getLocalServerPort() {
		return localServerPort;
	}

	public RemoteObjectWebSocketConnection setServiceInvoker(IRemoteObjectServiceInvoker serviceInvoker) {
		this.serviceInvoker = serviceInvoker;
		return this;
	}

	public RemoteObjectWebSocketConnection setLocalServerPort(int localServerPort) {
		this.localServerPort = localServerPort;
		return this;
	}

	public void start() {
		GWT.log("Opening new web socket");
		webSocket = WebSocket.create("ws://" + remoteAddress + "/remoteObjectWs", new WebSocket.Listener() {
			
			@Override
			public void onOpen(JavaScriptObject event) {
				requestRegistration();
			}
			
			@Override
			public void onMessage(String data) {
				packetReceived(data);
			}
			
			@Override
			public void onError(JavaScriptObject error) {
				GWT.log("Error: " + error);
			}
			
			@Override
			public void onClose(JavaScriptObject event) {
				GWT.log("Socket closed: " + event);
				start();
			}
		
		});
	}
	
	private void packetReceived(String rawPacket) {
		GWT.log("-> " + rawPacket);
		
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		String callbackId;
		switch (packet.getCommand()) {
		case 'A':
			break;
		case 'I': {
			packet.nextField(); // nodeId (ignored)
			callbackId = packet.nextField();
			Object value = serviceInvoker.invoke(packet);
			FlowerPlatformRemotingProtocolPacket resp = new FlowerPlatformRemotingProtocolPacket(securityToken, 'R');
			resp.addField(callbackId);
			resp.addField(value != null ? value.toString() : "");
			webSocket.sendMessage(resp.getRawData());
			break; }
		}
	}
	
	private void requestRegistration() {
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(securityToken, 'A');
		packet.addField(localNodeId);
		packet.addField("" + localServerPort);
		webSocket.sendMessage(packet.getRawData());
	}

}
