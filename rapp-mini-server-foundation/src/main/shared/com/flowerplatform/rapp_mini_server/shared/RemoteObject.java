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

	private static final char EOT = (char) 0x04;
	
	/**
	 * @see AbstractRemoteObjectInitializer
	 */
	protected IRequestSender requestSender;

	/**
	 * IP address or hostname of the board or hub which accepts requests.  
	 * <ul>
	 * <li>For <b>direct communication</b> (i.e. {@link #rappInstanceId} IS NOT used) is an IP address.</li>
	 * </li>
	 * </ul> 
	 * 
	 * @see #rappInstanceId
	 */
	protected String remoteAddress;
	
	/**
	 * Used only for direct communication w/ rapps of type "MCU" (e.g. Arduino). For rapps
	 * of type "mini server", it's not used, because authentication is managed at session level.
	 */
	protected String securityToken;
	
	/**
	 * For hub through communication.
	 * The rapp instance id towards which requests are dispatched (e.g. 
	 * <code>arduinoLivingRoom2</code> or <code>raspberryLivingRoom/lightsController</code>). 
	 * 
	 * @see #remoteAddress
	 */
	protected String rappInstanceId;
	
	protected String instanceName;
	
	public void setRequestSender(IRequestSender requestSender) {
		this.requestSender = requestSender;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setRemoteAddress(String destination) {
		this.remoteAddress = destination;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setRappInstanceId(String hubAddress) {
		this.rappInstanceId = hubAddress;
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
		
		StringBuilder sb = new StringBuilder();
		sb.append("FPRP\0"); // protocol header
		sb.append("1\0"); // protocol version
		sb.append(securityToken).append('\0'); // security token
		sb.append("I\0"); // command = INVOKE
		sb.append("0\0"); // hasNext = false
		sb.append(rappInstanceId).append('\0'); // rappInstanceId
		sb.append("\0"); // callbackId
		sb.append(instanceName.replaceAll("\\.",  ".\0")); // instanceName
		sb.append(method).append('\0'); // method
		
		for (Object o : arguments) {
			sb.append(o);
			sb.append('\0');
		}
		sb.append(EOT); // ASCII EOT
		requestSender.sendRequest("http://" + remoteAddress + "/hub", sb.toString(), new ResultCallback() {
			@Override
			public void run(Object result) {
				if (callback != null) {
					callback.run(result.toString());
				}
			}
		});
	}
	
}
