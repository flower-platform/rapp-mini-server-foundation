package com.crispico.flower_platform.remote_object.shared;

public interface IRemoteObjectServiceInvoker {
	
	public Object invoke(FlowerPlatformRemotingProtocolPacket packet);
	
}
