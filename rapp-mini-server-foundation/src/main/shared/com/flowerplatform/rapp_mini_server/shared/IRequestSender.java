package com.flowerplatform.rapp_mini_server.shared;

/**
 * @see AbstractRemoteObjectInitializer
 * 
 * @author Cristian Spiescu
 */
public interface IRequestSender {

	void sendRequest(String url, String payload, ResponseCallback callback);
	
}
