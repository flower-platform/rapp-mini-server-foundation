package com.crispico.flower_platform.remote_object.shared;

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
