package com.flowerplatform.rapp_mini_server.remote_object;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoteObjectHub {

	private Map<String, RemoteObjectHubClient> registeredClientsBySecurityToken = new ConcurrentHashMap<>();
	private Map<String, RemoteObjectHubClient> registeredClientsByNodeId= new ConcurrentHashMap<>();
	private Map<Integer, RemoteObjectHubClient> callbackIdCallerMap = new ConcurrentHashMap<>();
	
	private AtomicInteger lastCallbackId = new AtomicInteger(0);

	
	
	
}
