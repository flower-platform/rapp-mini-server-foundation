package com.flowerplatform.rapp_mini_server.remote_object;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RemoteObjectHubClientInfo {

	private String rappInstanceId;

	private long lastActivityTimestamp;
	
	private Queue<String> pendingInvocations = new ConcurrentLinkedQueue<>();

	private Queue<String> pendingResponses = new ConcurrentLinkedQueue<>();
	
	public RemoteObjectHubClientInfo(String rappInstanceId) {
		this.rappInstanceId = rappInstanceId;
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
