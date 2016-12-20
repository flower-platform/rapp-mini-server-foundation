package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.DataInputStream;
import java.io.IOException;
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
		if (client == null && packet.getCommand() != 'A') {
//			return;
		}
//		client.setLastActivityTimestamp(System.currentTimeMillis());
		
		String rappInstanceId;
		FlowerPlatformRemotingProtocolPacket res = null;

		switch (packet.getCommand()) {
		case 'A': // register
			rappInstanceId = packet.nextField();
			RemoteObjectHubClient newClient = new RemoteObjectHubClient(rappInstanceId);
			registeredClientsBySecurityToken.put(packet.getSecurityToken(), newClient);
			registeredClientsByRappInstanceId.put(rappInstanceId, newClient);
			addDummyInvocations(newClient);
			break;
		case 'J': // get pending invocations
		case 'R': // result
			if (client.getPendingInvocations().size() == 0) {
				break;
			}
			String invocation = client.getPendingInvocations().remove();
			res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'I');
			res.addField(client.getPendingInvocations().size() > 0 ? "1" : "0"); // hasNext
			res.addField(""); // rappInstanceId
			res.addField(invocation); // includes callbackId
			break;
		case 'I': // invoke
			packet.nextField(); // hasNext (ignored);
			rappInstanceId = packet.nextField(); // rappInstanceId
			RemoteObjectHubClient invokedClient = registeredClientsByRappInstanceId.get(rappInstanceId);
			if (invokedClient == null) {
				break;
			}
			int callbackId = lastCallbackId.incrementAndGet();
			StringBuilder pendingInvocation = new StringBuilder(lastCallbackId + "\0");
			while (packet.availableFields() > 0) {
				pendingInvocation.append(packet.nextField()).append("\0");
			}
			invokedClient.addPendingInvocation(pendingInvocation.toString());
			res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'P');
			res.addField("" + callbackId);
		}

		if (res == null) {
			res = new FlowerPlatformRemotingProtocolPacket(packet.getSecurityToken(), 'A');
		}
		
		response.getWriter().print(res.getRawData());
	}
	
	private static void addDummyInvocations(RemoteObjectHubClient client) {
		client.getPendingInvocations().add("120\0lightController1.\0sayHello\0Test\0" + "2\0");
		client.getPendingInvocations().add("121\0lightController1.\0turnOn\0");
		client.getPendingInvocations().add("122\0lightController1.\0turnOff\0room1\0");
	}
	
}
