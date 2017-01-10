package com.flowerplatform.rapp_mini_server.shared;

public interface IRemoteObjectServiceInvoker {
	
	public Object invoke(FlowerPlatformRemotingProtocolPacket packet);
	
}
