package com.crispico.flower_platform.remote_object;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RemoteObjectHubClientData {
	
	private int clientType;
	
	private String nodeId;

	private String securityToken;
	
	private long lastActivityTimestamp;
	
	private Queue<String> pendingInvocations = new ConcurrentLinkedQueue<>();

	private Queue<String> pendingResponses = new ConcurrentLinkedQueue<>();
	
	private String remoteIPAddress;
	
	private int remoteHttpServerPort = 0;
	
	private RemoteObjectWebSocketHandler webSocket;
	
	public RemoteObjectHubClientData(int clientType, String nodeId, String securityToken) {
		this.clientType = clientType;
		this.nodeId = nodeId;
		this.securityToken = securityToken;
	}

	public int getClientType() {
		return clientType;
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getSecurityToken() {
		return securityToken;
	}
	
	public String getRemoteIPAddress() {
		return remoteIPAddress;
	}

	public void setRemoteIPAddress(String remoteAddress) {
		this.remoteIPAddress = remoteAddress;
	}

	public int getRemoteHttpServerPort() {
		return remoteHttpServerPort;
	}

	public void setRemoteHttpServerPort(int remoteServerPort) {
		this.remoteHttpServerPort = remoteServerPort;
	}

	public RemoteObjectWebSocketHandler getWebSocket() {
		return webSocket;
	}

	public void setWebSocket(RemoteObjectWebSocketHandler webSocket) {
		this.webSocket = webSocket;
	}

	public void addPendingInvocation(String invocation) {
		pendingInvocations.add(invocation);
	}

	public void addPendingResponse(String response) {
		pendingResponses.add(response);
	}

	public Queue<String> getPendingInvocations() {
		return pendingInvocations;
	}

	public Queue<String> getPendingResponses() {
		return pendingResponses;
	}

	public long getLastActivityTimestamp() {
		return lastActivityTimestamp;
	}
	
	public void setLastActivityTimestamp(long lastActivityTimestamp) {
		this.lastActivityTimestamp = lastActivityTimestamp;
	}

}
