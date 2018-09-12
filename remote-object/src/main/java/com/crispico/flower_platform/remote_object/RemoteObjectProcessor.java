package com.crispico.flower_platform.remote_object;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;

public class RemoteObjectProcessor {
	
	private String securityToken;
	
	private RemoteObjectServiceInvoker serviceInvoker;
	
	public RemoteObjectProcessor(String securityToken, RemoteObjectServiceInvoker serviceInvoker) {
		this.securityToken = securityToken;
		this.serviceInvoker = serviceInvoker;
	}
	
	public FlowerPlatformRemotingProtocolPacket processPacket(FlowerPlatformRemotingProtocolPacket packet)	throws JsonProcessingException {
		switch (packet.getCommand()) {
		case 'I':
			packet.nextField(); // nodeId (ignore)
			String callbackId = packet.nextField(); // callbackId

			Object result = serviceInvoker.invoke(packet);

			FlowerPlatformRemotingProtocolPacket res = new FlowerPlatformRemotingProtocolPacket(securityToken, 'R');
			res.addField(callbackId); // callbackId

			String ret;
			if (result == null) {
				ret = "";
			} else if (result.getClass().isPrimitive() || result instanceof String) {
				ret = result.toString();
			} else {
				ret = RemoteObjectServiceInvoker.getObjectMapper().writeValueAsString(result);
			}
			res.addField(ret);
			return res;
		}
		return null;
	}
	
}
