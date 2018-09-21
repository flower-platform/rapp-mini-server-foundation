package com.crispico.flower_platform.remote_object.shared;

public interface IRequestSender {

	void sendRequest(String url, String payload, ResponseCallback callback);
	
}
