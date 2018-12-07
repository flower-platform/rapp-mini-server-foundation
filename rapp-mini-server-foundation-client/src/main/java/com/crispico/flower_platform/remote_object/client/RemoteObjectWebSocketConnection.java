package com.crispico.flower_platform.remote_object.client;

import com.crispico.flower_platform.remote_object.shared.FlowerPlatformRemotingProtocolPacket;
import com.crispico.flower_platform.remote_object.shared.IRemoteObjectServiceInvoker;
import com.crispico.flower_platform.remote_object.shared.RemoteObject;
import com.crispico.flower_platform.remote_object.shared.ResponseCallback;
import com.crispico.flower_platform.remote_object.shared.ResultCallback;
import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsType;

@JsType(namespace="rapp_mini_server")
public class RemoteObjectWebSocketConnection {

	private WebSocket webSocket; 

	private boolean started = false;
	
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

	private ResponseCallback callback;
	
	public String getRemoteAddress() {
		return remoteAddress;
	}

	public RemoteObjectWebSocketConnection setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
		return this;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public RemoteObjectWebSocketConnection setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
		return this;
	}

	public String getLocalNodeId() {
		return localNodeId;
	}

	public RemoteObjectWebSocketConnection setLocalNodeId(String localNodeId) {
		this.localNodeId = localNodeId;
		return this;
	}

	public IRemoteObjectServiceInvoker getServiceInvoker() {
		return serviceInvoker;
	}

	public RemoteObjectWebSocketConnection setServiceInvoker(IRemoteObjectServiceInvoker serviceInvoker) {
		this.serviceInvoker = serviceInvoker;
		return this;
	}

	public void start(ResponseCallback callback) {
		log("Opening new web socket ");
		this.callback = callback;
		webSocket = WebSocket.create("ws://" + remoteAddress + "/remoteObjectWs", new WebSocket.Listener() {
			
			@Override
			public void onOpen(JavaScriptObject event) {
				started = true;
				requestRegistration();
				if (callback != null) {
					callback.onSuccess(null);
				}
			}
			
			@Override
			public void onMessage(String data) {
				packetReceived(data);
			}
			
			@Override
			public void onError(JavaScriptObject error) {
				if (callback != null && !started) {
					callback.onError(error.toString());
				}
				log("Error: " + error);
			}
			
			@Override
			public void onClose(JavaScriptObject event) {
				log("Socket closed: " + event + " " + started);
				if (started) {
					start(null);
				}
			}
		
		});
	}
	
	private void packetReceived(String rawPacket) {
		log("-> " + rawPacket);
		
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
		case 'R': {
			callbackId = packet.nextField();
			String valueStr = packet.nextField();
			ResultCallback callback = RemoteObject.getCallbacks().get(callbackId);
			if (callback != null) {
				callback.run(valueStr);
			}
			break; }
		}
	}
	
	private void requestRegistration() {
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(securityToken, 'A');
		packet.addField(localNodeId);
		packet.addField(""); // no server port
		webSocket.sendMessage(packet.getRawData());
	}

	public void stop() {
		started = false;
		if (webSocket != null) {
			webSocket.close();
		}
	}

	public static native final void log(String s) /*-{
		console.log(s);
	}-*/;

}
