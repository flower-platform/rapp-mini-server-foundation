package com.flowerplatform.rapp_mini_server.remote_object;

import java.lang.reflect.Method;

/**
 * @author Andrei Taras
 */
public class RemoteObjectInfo {
	
	private Object remoteObject;
	
	private Method method;
	
	/**
	 * The arguments values for the actual service invocation.
	 */
	private Object[] arguments;
	
	public Object getRemoteObject() {
		return remoteObject;
	}

	public void setRemoteObject(Object service) {
		this.remoteObject = service;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public Object[] getArguments() {
		return arguments;
	}
	
	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

}
