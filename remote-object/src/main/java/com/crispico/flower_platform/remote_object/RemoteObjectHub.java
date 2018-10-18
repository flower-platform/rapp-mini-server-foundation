package com.crispico.flower_platform.remote_object;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.crispico.flower_platform.remote_object.shared.FlowerPlatformRemotingProtocolPacket;

public class RemoteObjectHub {

	private static RemoteObjectHub singletonInstance;
	
	private Map<String, RemoteObjectHubClient> registeredClientsBySecurityToken = new ConcurrentHashMap<>();
	private Map<String, RemoteObjectHubClient> registeredClientsByNodeId = new ConcurrentHashMap<>();
	private Map<Integer, RemoteObjectHubClient> callbackIdCallerMap = new ConcurrentHashMap<>();

	private AtomicInteger lastCallbackId = new AtomicInteger(0);

	private RemoteObjectHub() {
		
	}
	
	public String registerClient(RemoteObjectHubClient client) {
		// TODO CM: check nodeId/securityToken pair

		registeredClientsBySecurityToken.put(client.getSecurityToken(), client);
		registeredClientsByNodeId.put(client.getNodeId(), client);
		return new FlowerPlatformRemotingProtocolPacket(client.getSecurityToken(), 'A').getRawData();
	}
	
	private FlowerPlatformRemotingProtocolPacket invokeHttpPushClient(RemoteObjectHubClient invokedClient, String invocation, String invokerSecurityToken) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://" + invokedClient.getRemoteIPAddress()+ ":" + invokedClient.getRemoteHttpServerPort()).openConnection();
			FlowerPlatformRemotingProtocolPacket pak = new FlowerPlatformRemotingProtocolPacket(invokedClient.getSecurityToken(), 'I');
			pak.addField(""); // nodeId
			pak.addField(invocation);
			conn.setDoOutput(true);
			System.out.println("PUSH <- " + pak.getRawData());
			conn.getOutputStream().write(pak.getRawData().getBytes());
	
			byte[] respBuf = new byte[conn.getContentLength()];
			DataInputStream respIn = new DataInputStream(conn.getInputStream());
			respIn.readFully(respBuf);
			respIn.close();
			String rawRespPacket = new String(respBuf);
			System.out.println("PUSH -> " + rawRespPacket);
			FlowerPlatformRemotingProtocolPacket respPacket = new FlowerPlatformRemotingProtocolPacket(rawRespPacket);
			if (respPacket.getCommand() != 'R') {
				return new FlowerPlatformRemotingProtocolPacket(invokerSecurityToken, 'X');
			}
			respPacket.nextField(); // callbackId (ignored)
			String value = respPacket.nextField();
			FlowerPlatformRemotingProtocolPacket res = new FlowerPlatformRemotingProtocolPacket(invokerSecurityToken, 'R');
			res.addField("0"); // callbackId
			res.addField(value);
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			return new FlowerPlatformRemotingProtocolPacket(invokerSecurityToken, 'X');
		}
	}
	
	public String processPacket(FlowerPlatformRemotingProtocolPacket packet) {
		// retrieve registered client
		RemoteObjectHubClient client = registeredClientsBySecurityToken.get(packet.getSecurityToken());
		if (client != null) {
			client.setLastActivityTimestamp(System.currentTimeMillis());
		} else {
//			return new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'X').getRawData();
		}
		
		String nodeId;
		FlowerPlatformRemotingProtocolPacket res = null;

		switch (packet.getCommand()) {
		case 'I': { // invoke
			nodeId = packet.nextField(); // nodeId
			packet.nextField(); // callbackId (ignored)
			RemoteObjectHubClient invokedClient = registeredClientsByNodeId.get(nodeId);
			if (invokedClient == null) {
				break;
			}
			
			int callbackId = 0;
			if (client != null) {
				// get new callbackId
				if (lastCallbackId.get() >= 0xFFFF) {
					lastCallbackId.set(0);
				}
				callbackId = lastCallbackId.incrementAndGet();
				callbackIdCallerMap.put(callbackId, client);
			}
			
			// build invocation string
			StringBuilder sbInvocation = new StringBuilder(callbackId + "\0");
			while (packet.availableFieldCount() > 0) {
				sbInvocation.append(packet.nextField()).append("\0");
			}
			String invocation = sbInvocation.toString();

			// invoke or queue invocation, depending on client type
			switch (invokedClient.getClientType()) {
			case RemoteObjectHubClient.CLIENT_TYPE_HTTP_PUSH:
				res = invokeHttpPushClient(invokedClient, invocation, packet.getSecurityToken());
				break;
			case RemoteObjectHubClient.CLIENT_TYPE_HTTP_PULL:
				invokedClient.addPendingInvocation(sbInvocation.toString());
				res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'P');
				res.addField("" + callbackId);
				break;
			case RemoteObjectHubClient.CLIENT_TYPE_WEB_SOCKET:
				FlowerPlatformRemotingProtocolPacket invokePacket = new FlowerPlatformRemotingProtocolPacket(invokedClient.getSecurityToken(), 'I');
				invokePacket.addField(invokedClient.getNodeId()); // nodeId
				invokePacket.addField(invocation);
				try {
					res = invokedClient.getWebSocket().sendReceiveSynchronously(invokePacket);
					res.setSecurityToken(packet.getSecurityToken());
				} catch (IOException e) { 
					e.printStackTrace();
				}
				break;
			}
			break; }
		case 'R':  // result received
			int callbackId = Integer.parseInt(packet.nextField()); // callbackId
			RemoteObjectHubClient invokerClient = callbackIdCallerMap.remove(callbackId);
			if (invokerClient == null) {
				res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'X');
				break;
			}
			String result = packet.nextField(); // value
			invokerClient.addPendingResponse(callbackId + "\0" + result); 
			// no break here; continue with 'J' command, for HTTP PULL clients
		case 'J': // "get pending invocations" command received
			if (client.getPendingInvocations().size() == 0) {
				res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'J');
				break;
			}
			String invocation = client.getPendingInvocations().remove();
			res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'I');
			res.addField(""); // nodeId
			res.addField(invocation); // includes callbackId
			break;
		case 'S': // get pending results
			if (client.getPendingResponses().size() == 0) {
				res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'S');
				break;
			}
			StringBuilder sbRes = new StringBuilder();
			while (client.getPendingResponses().size() > 0) {
				String pendingResponse = client.getPendingResponses().remove();
				res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'R');
				res.addField(pendingResponse); // includes callbackId
				sbRes.append(res.getRawData());
			}
			System.out.println("<- " + sbRes.toString());
			return sbRes.toString();
		}	
		
		if (res == null) {
			res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'X');
		}
		
		return res.getRawData();
	}
	
	
	public static RemoteObjectHub getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new RemoteObjectHub();
		}
		return singletonInstance;
	}
	
}
