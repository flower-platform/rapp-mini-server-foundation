package com.flowerplatform.rapp_mini_server.shared;

import java.util.HashMap;
import java.util.Map;

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

	private static final char TERM = (char) 0x00;
	
	private static Map<String, ResultCallback> callbacks = new HashMap<>();
	
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

	public static Map<String, ResultCallback> getCallbacks() {
		return callbacks;
	}

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
		sb.append("FPRP").append(TERM); // protocol header
		sb.append("1").append(TERM); // protocol version
		sb.append(securityToken).append(TERM); // security token
		sb.append("I").append(TERM); // command = INVOKE
		sb.append("0").append(TERM); // hasNext = false
		sb.append(rappInstanceId == null ? "" : rappInstanceId).append(TERM); // rappInstanceId
		sb.append(TERM); // callbackId (null)
		if (instanceName != null) {
			sb.append(instanceName).append('.'); // instanceName
		}
		sb.append(method).append(TERM); // method
		
		for (Object o : arguments) {
			sb.append(o);
			sb.append(TERM);
		}
		sb.append(EOT); // ASCII EOT
		requestSender.sendRequest("http://" + remoteAddress + (rappInstanceId == null ? "/remoteObject" : "/hub"), sb.toString(), new RemoteObjectResponseCallback(callback));
	}

	final class RemoteObjectResponseCallback implements ResponseCallback {

		public ResultCallback clientCallback;
		
		public RemoteObjectResponseCallback(ResultCallback clientCallback) {
			this.clientCallback = clientCallback;
		}

		@Override
		public void onSuccess(Object result) {
			if (clientCallback == null) {
				return;
			}
			FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(result.toString());
			switch(packet.getCommand()) {
			case 'R':
				packet.nextField(); // hasNext (ignored)
				packet.nextField(); // callbackId
				String value = packet.nextField(); // result value
				clientCallback.run(value);
				break;
			case 'P':
				String callbackId = packet.nextField(); // callbackId
				callbacks.put(callbackId, clientCallback);
				break;
			}
		}
	
		public void onError() {
			
		}
		
	}

}
