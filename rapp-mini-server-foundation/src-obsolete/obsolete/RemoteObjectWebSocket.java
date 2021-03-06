package com.flowerplatform.rapp_mini_server.remote_object.obsolete;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import com.crispico.flower_platform.remote_object.RemoteObjectHub;
import com.crispico.flower_platform.remote_object.RemoteObjectHubClient;
import com.crispico.flower_platform.remote_object.shared.FlowerPlatformRemotingProtocolPacket;

public class RemoteObjectWebSocket extends WebSocketAdapter {

	private DateFormat df = new SimpleDateFormat("HH:mm:ss.S");

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
		System.out.println(String.format("[%s] %s -> %s", df.format(new Date()), getRemote().getInetSocketAddress().getAddress().getHostAddress(), rawPacket));
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		if (packet.getCommand() != 'A') {
			this.lastPacket = packet;
			synchronized(this) { this.notifyAll(); }
			return;
		}
		String nodeId = packet.nextField();
		RemoteObjectHubClient client = new RemoteObjectHubClient(RemoteObjectHubClient.CLIENT_TYPE_WEB_SOCKET, nodeId, packet.getSecurityToken());
		
		//TODO CM: incompatibility
//		client.setWebSocket(this);
		
		hub.registerClient(client);
	}

	public synchronized FlowerPlatformRemotingProtocolPacket sendReceiveSynchronously(FlowerPlatformRemotingProtocolPacket packet) throws IOException {
		System.out.print(String.format("[%s] %s <- %s", df.format(new Date()), getRemote().getInetSocketAddress().getAddress().getHostAddress(), packet.getRawData()));
		if (!isConnected()) {
			return null;
		}
		getRemote().sendString(packet.getRawData());
		System.out.println("\t[SENT]");
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