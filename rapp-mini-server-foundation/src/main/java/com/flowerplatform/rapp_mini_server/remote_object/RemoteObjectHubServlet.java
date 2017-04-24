package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;

public class RemoteObjectHubServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private Map<String, RemoteObjectHubClient> registeredClientsBySecurityToken = new ConcurrentHashMap<>();
	private Map<String, RemoteObjectHubClient> registeredClientsByRappInstanceId= new ConcurrentHashMap<>();
	private Map<Integer, RemoteObjectHubClient> callbackIdCallerMap = new ConcurrentHashMap<>();
	
	private AtomicInteger lastCallbackId = new AtomicInteger(0);
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getContentLength() == 0) {
			return;
		}
		byte[] buf = new byte[request.getContentLength()];
		DataInputStream in = new DataInputStream(request.getInputStream());
		in.readFully(buf);
		String rawPacket = new String(buf);
		
		System.out.println("-> " + rawPacket);
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		
		// retrieve registered client
		RemoteObjectHubClient client = registeredClientsBySecurityToken.get(packet.getSecurityToken());
		if (client != null) {
			client.setLastActivityTimestamp(System.currentTimeMillis());
		} else if (packet.getCommand() != 'A') {
			return;
		}
		
		String rappInstanceId;
		FlowerPlatformRemotingProtocolPacket res = null;

		switch (packet.getCommand()) {
		case 'A': // register
			rappInstanceId = packet.nextField();
			if (client == null) {
				client = new RemoteObjectHubClient(rappInstanceId, packet.getSecurityToken());
				registeredClientsBySecurityToken.put(packet.getSecurityToken(), client);
				registeredClientsByRappInstanceId.put(rappInstanceId, client);
			}
			client.setRemoteAddress(request.getRemoteAddr());
			String portStr = packet.nextField();
			if (portStr.length() > 0) {
				client.setRemoteServerPort(Integer.parseInt(portStr));
			}

			res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'A');
//			addDummyInvocations(client);
			break;
		case 'I': { // invoke
			rappInstanceId = packet.nextField(); // rappInstanceId
			packet.nextField(); // callbackId (ignored)
			RemoteObjectHubClient invokedClient = registeredClientsByRappInstanceId.get(rappInstanceId);
			if (invokedClient == null) {
				break;
			}
			
			if (lastCallbackId.get() >= 0xFFFF) {
				lastCallbackId.set(0);
			}
			int callbackId = lastCallbackId.incrementAndGet();
			callbackIdCallerMap.put(callbackId, client);
			StringBuilder sbInvocation = new StringBuilder(callbackId + "\0");
			while (packet.availableFieldCount() > 0) {
				sbInvocation.append(packet.nextField()).append("\0");
			}
			String invocation = sbInvocation.toString();
			
			if (invokedClient.getRemoteServerPort() > 0) { // client is in "push" mode
				HttpURLConnection conn = (HttpURLConnection) new URL("http://" + invokedClient.getRemoteAddress()+ ":" + invokedClient.getRemoteServerPort()).openConnection();
				FlowerPlatformRemotingProtocolPacket pak = new FlowerPlatformRemotingProtocolPacket(invokedClient.getSecurityToken(), 'I');
				pak.addField(""); // rappInstanceId
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
					break;
				}
				respPacket.nextField(); // callbackId (ignored)
				String value = respPacket.nextField();
				res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'R');
				res.addField("0"); // callbackId
				res.addField(value);
			} else { // client is in "pull" mode
				invokedClient.addPendingInvocation(sbInvocation.toString());
				res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'P');
				res.addField("" + callbackId);
			}
			break; }
		case 'J': // "get pending invocations" command received
		case 'R': { // result received
			if (packet.getCommand() == 'R') {
				int callbackId = Integer.parseInt(packet.nextField()); // callbackId
				RemoteObjectHubClient invokerClient = callbackIdCallerMap.remove(callbackId);
				if (invokerClient != null) {
					String value = packet.nextField(); // value
					invokerClient.addPendingResponse(callbackId + "\0" + value);
				}
			}

			if (client.getPendingInvocations().size() == 0) {
				res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'J');
				break;
			}
			String invocation = client.getPendingInvocations().remove();
			res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'I');
			res.addField(""); // rappInstanceId
			res.addField(invocation); // includes callbackId
			break; }
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
			response.getWriter().print(sbRes.toString());
			return;
		}	
		
		if (res == null) {
			res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'X');
		}
		
		System.out.println("<- " + res.getRawData());
		response.getWriter().print(res.getRawData());
	}
	
}
