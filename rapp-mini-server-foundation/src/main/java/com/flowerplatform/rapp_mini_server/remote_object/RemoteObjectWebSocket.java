package com.flowerplatform.rapp_mini_server.remote_object;

import static com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket.TERM;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;

public class RemoteObjectWebSocket extends WebSocketAdapter {

	private RemoteObjectProcessor processor;
	
	public RemoteObjectWebSocket(RemoteObjectProcessor processor) {
		this.processor = processor;
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
		FlowerPlatformRemotingProtocolPacket resp;
		if (packet.getCommand() == 'A') {
//			resp = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'A');
			
			//test
			String securityToken = "12345678";
			String nodeId = null;
			String instanceName = "testService";
			String method = "jsHello";
			Object[] arguments = new Object[] { "world" };
			
			StringBuilder sb = new StringBuilder();
			sb.append("FPRP").append(TERM); // protocol header
			sb.append("1").append(TERM); // protocol version
			sb.append(securityToken).append(TERM); // security token
			sb.append("I").append(TERM); // command = INVOKE
			sb.append(nodeId == null ? "" : nodeId).append(TERM); // nodeId
			sb.append(TERM); // callbackId (null)
			if (instanceName != null && instanceName.length() > 0) {
				sb.append(instanceName).append('.'); // instanceName
			}
			sb.append(method).append(TERM); // method
			
			for (Object o : arguments) {
				sb.append(o);
				sb.append(TERM);
			}
			sb.append(FlowerPlatformRemotingProtocolPacket.EOT); // ASCII EOT
			
			resp = new FlowerPlatformRemotingProtocolPacket(sb.toString());
			try {
				super.getRemote().sendString(resp.getRawData());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (packet.getCommand() == 'I') {
			try {
				resp = processor.processPacket(packet);
				super.getRemote().sendString(resp.getRawData());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		System.out.println("Socket Closed: [" + statusCode + "] " + reason);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		super.onWebSocketError(cause);
		cause.printStackTrace(System.err);
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		super.onWebSocketBinary(payload, offset, len);
		System.out.println(new String(payload, offset, len));
	}

}