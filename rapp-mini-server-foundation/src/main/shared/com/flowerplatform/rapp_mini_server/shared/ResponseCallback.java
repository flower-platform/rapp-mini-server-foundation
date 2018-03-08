package com.flowerplatform.rapp_mini_server.shared;

import jsinterop.annotations.JsType;

/**
 * 
 * @author Claudiu Matei
 */
@JsType(namespace="rapp_mini_server")
public interface ResponseCallback {

	void onSuccess(Object response);
	
	void onError(String message);
	
}
