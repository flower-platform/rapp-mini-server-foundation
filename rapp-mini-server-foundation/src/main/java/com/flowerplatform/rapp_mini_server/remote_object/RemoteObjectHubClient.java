package com.flowerplatform.rapp_mini_server.remote_object;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RemoteObjectHubClient {

	private String rappInstanceId;

	private String securityToken;
	
	private long lastActivityTimestamp;
	
	private Queue<String> pendingInvocations = new ConcurrentLinkedQueue<>();

	private Queue<String> pendingResponses = new ConcurrentLinkedQueue<>();
	
	private String remoteAddress;
	
	private int remoteServerPort = 0;
	
	public RemoteObjectHubClient(String rappInstanceId, String securityToken) {
		this.rappInstanceId = rappInstanceId;
		this.securityToken = securityToken;
	}

	public String getSecurityToken() {
		return securityToken;
	}
	
	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public int getRemoteServerPort() {
		return remoteServerPort;
	}

	public void setRemoteServerPort(int remoteServerPort) {
		this.remoteServerPort = remoteServerPort;
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

	public String getRappInstanceId() {
		return rappInstanceId;
	}

}
