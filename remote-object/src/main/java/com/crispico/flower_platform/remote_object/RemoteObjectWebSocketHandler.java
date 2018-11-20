package com.crispico.flower_platform.remote_object;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.websocket.Session;

import com.crispico.flower_platform.remote_object.shared.FlowerPlatformRemotingProtocolPacket;

public class RemoteObjectWebSocketHandler {

	private DateFormat df = new SimpleDateFormat("HH:mm:ss.S");

	private RemoteObjectHub hub;
	
	private FlowerPlatformRemotingProtocolPacket lastPacket;
	
	private Session session;
	
	public RemoteObjectWebSocketHandler(Session session) {
		this.session = session;
	}

	public void onWebSocketText(String rawPacket) {
		System.out.println(String.format("[%s] %s -> %s", df.format(new Date()), session.getId(), rawPacket));
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		if (packet.getCommand() != 'A') {
			this.lastPacket = packet;
			synchronized(this) { this.notifyAll(); }
			return;
		}
		String nodeId = packet.nextField();
		RemoteObjectHubClientData client = new RemoteObjectHubClientData(RemoteObjectHubClientData.CLIENT_TYPE_WEB_SOCKET, nodeId, packet.getSecurityToken());
		client.setWebSocket(this);
		hub.registerClient(client);
	}

	public synchronized FlowerPlatformRemotingProtocolPacket sendReceiveSynchronously(FlowerPlatformRemotingProtocolPacket packet) throws IOException {
		System.out.print(String.format("[%s] %s <- %s", df.format(new Date()), session.getId(), packet.getRawData()));
		if (!session.isOpen()) {
			return null;
		}
		session.getBasicRemote().sendText(packet.getRawData());
		System.out.println("\t[SENT]");
		lastPacket = null;
		try { this.wait(5000); } catch (InterruptedException e) { }
		return lastPacket;
	}

	public void onWebSocketClose() {
		synchronized(this) { this.notifyAll(); }
	}

	public void onWebSocketError(Throwable cause) {
		synchronized(this) { this.notifyAll(); }
		cause.printStackTrace(System.err);
	}

	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		synchronized(this) { this.notifyAll(); }
		System.out.println(new String(payload, offset, len));
	}

}