package com.flowerplatform.rapp_mini_server.shared;

public interface IRequestSender {

	void sendRequest(String url, String payload, ResponseCallback callback);
	
}
