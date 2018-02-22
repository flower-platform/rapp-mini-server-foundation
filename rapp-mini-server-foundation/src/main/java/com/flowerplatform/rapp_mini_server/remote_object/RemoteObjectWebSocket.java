package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;

public class RemoteObjectWebSocket extends WebSocketAdapter {

	private RemoteObjectHub hub;
	
	private FlowerPlatformRemotingProtocolPacket lastPacket;
	
	public RemoteObjectWebSocket(RemoteObjectHub hub) {
		this.hub = hub;
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		System.out.println("Socket Connected: " + sess);
	}

	@Override
	public void onWebSocketText(String rawPacket) {
		System.out.println("-> " + rawPacket);
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		if (packet.getCommand() != 'A') {
			this.lastPacket = packet;
			synchronized(this) { this.notifyAll(); }
			return;
		}
		String nodeId = packet.nextField();
		RemoteObjectHubClient client = new RemoteObjectHubClient(RemoteObjectHubClient.CLIENT_TYPE_WEB_SOCKET, nodeId, packet.getSecurityToken());
		client.setWebSocket(this);
		hub.registerClient(client);
	}

	public synchronized FlowerPlatformRemotingProtocolPacket sendReceiveSynchronously(FlowerPlatformRemotingProtocolPacket packet) throws IOException {
		if (!isConnected()) {
			return null;
		}
		getRemote().sendString(packet.getRawData());
		lastPacket = null;
		try { this.wait(5000); } catch (InterruptedException e) { }
		return lastPacket;
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		synchronized(this) { this.notifyAll(); }
		System.out.println("Socket Closed: [" + statusCode + "] " + reason);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		super.onWebSocketError(cause);
		synchronized(this) { this.notifyAll(); }
		cause.printStackTrace(System.err);
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		super.onWebSocketBinary(payload, offset, len);
		synchronized(this) { this.notifyAll(); }
		System.out.println(new String(payload, offset, len));
	}

}