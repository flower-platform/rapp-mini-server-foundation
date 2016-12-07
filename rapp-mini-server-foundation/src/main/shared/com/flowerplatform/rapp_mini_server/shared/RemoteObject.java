package com.flowerplatform.rapp_mini_server.shared;

import jsinterop.annotations.JsType;

/**
 * For Java, this class is meant to be sub-classed. For JS/GWT, a JS Proxy
 * is instantiated containing an instance of this class.
 * 
 * @author Cristian Spiescu
 */
@JsType(namespace="rapp_mini_server")
public class RemoteObject {

	/**
	 * @see AbstractRemoteObjectInitializer
	 */
	protected IRequestSender requestSender;

	/**
	 * <ul>
	 * <li>For <b>direct communication</b> (i.e. {@link #hubAddress} IS NOT used) is an IP address.</li>
	 * <li>For <b>hub pass-through communication</b> (i.e. {@link #hubAddress} IS used) is a rappInstanceId (e.g. 
	 * <code>arduinoLivingRoom2</code> or <code>raspberryLivingRoom/lightsController</code>). 
	 * </li>
	 * </ul> 
	 * 
	 * @see #hubAddress
	 */
	protected String destination;
	
	/**
	 * Used only for direct communication w/ rapps of type "MCU" (e.g. Arduino). For rapps
	 * of type "mini server", it's not used, because authentication is managed at session level.
	 */
	protected String securityToken;
	
	/**
	 * For hub through communication: the IP of the rapp of type "mini server" that is used as hub/dispatcher.
	 * 
	 * @see #destination
	 */
	protected String hubAddress;
	
	protected String instanceName;
	
	public void setRequestSender(IRequestSender requestSender) {
		this.requestSender = requestSender;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setDestination(String destination) {
		this.destination = destination;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setHubAddress(String hubAddress) {
		this.hubAddress = hubAddress;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setInstanceName(String instance) {
		this.instanceName = instance;
		return (T) this;
	}
	
	public void invokeMethod(String method, Object[] arguments, ResultCallback callback) {
		if (requestSender == null) {
			throw new IllegalStateException("The RemoteObject is not initialized; i.e. requestSender is null. It should be initialized by a *RemoteObjectInitializer.");
		}
		
		// TODO: de inlocuit cu protocolul
		StringBuilder sb = new StringBuilder();
		for (Object o : arguments) {
			sb.append(o);
			sb.append(",");
		}
		requestSender.sendRequest("url", "For destination: " + destination + ", calling: " + instanceName + "." + method + "(" + sb + ")");
		if (callback != null) {
			callback.run("this is a result");
		}
	}
	
}
