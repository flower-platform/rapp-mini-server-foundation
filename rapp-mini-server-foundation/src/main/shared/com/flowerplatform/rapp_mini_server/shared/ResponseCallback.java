package com.flowerplatform.rapp_mini_server.shared;

/**
 * 
 * @author Claudiu Matei
 */
public interface ResponseCallback {

	void onSuccess(Object response);
	
	void onError(String message);
	
}
