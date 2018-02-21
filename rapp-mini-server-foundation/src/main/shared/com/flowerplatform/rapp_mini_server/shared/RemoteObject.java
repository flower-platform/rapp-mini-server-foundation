package com.flowerplatform.rapp_mini_server.shared;

import static com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket.EOT;
import static com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket.TERM;

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

	private static Map<String, ResultCallback> callbacks = new HashMap<>();
	
	protected IRequestSender requestSender;

	/**
	 * IP address or hostname of the board or hub which accepts requests.  
	 * <ul>
	 * <li>For <b>direct communication</b> (i.e. {@link #nodeId} IS NOT used) is an IP address.</li>
	 * </li>
	 * </ul> 
	 * 
	 * @see #nodeId
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
	protected String nodeId;
	
	protected String instanceName;

	public static Map<String, ResultCallback> getCallbacks() {
		return callbacks;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setRequestSender(IRequestSender requestSender) {
		this.requestSender = requestSender;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setRemoteAddress(String destination) {
		this.remoteAddress = destination;
		return (T) this;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
		return (T) this;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setNodeId(String remoteNodeId) {
		this.nodeId = remoteNodeId;
		return (T) this;
	}

	public String getNodeId() {
		return nodeId;
	}
	

	@SuppressWarnings("unchecked")
	public <T extends RemoteObject> T setInstanceName(String instance) {
		this.instanceName = instance;
		return (T) this;
	}

	public String getInstanceName() {
		return instanceName;
	}
	
	public void invokeMethod(String method, Object[] arguments, ResultCallback callback) {
		if (requestSender == null) {
			throw new IllegalStateException("The RemoteObject is not initialized; i.e. requestSender is null. It should be initialized by an IRemoteObjectInitializer.");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("FPRP").append(TERM); // protocol header
		sb.append("1").append(TERM); // protocol version
		sb.append(securityToken).append(TERM); // security token
		sb.append("I").append(TERM); // command = INVOKE
		sb.append(nodeId == null ? "" : nodeId).append(TERM); // nodeId
		sb.append(TERM); // callbackId (null)
		if (instanceName != null && instanceName.length() > 0) {
			sb.append(instanceName).append('.'); // instanceName
		}
		sb.append(method).append(TERM); // method
		
		for (Object o : arguments) {
			sb.append(o);
			sb.append(TERM);
		}
		sb.append(EOT); // ASCII EOT
		String url;
		if (nodeId == null || nodeId.length() == 0) {
			url = "remoteObject";
		} else if (nodeId.startsWith("serial/")) {
			url = "serialBus";
		} else {
			url = "hub";
		}
		requestSender.sendRequest("http://" + remoteAddress + "/" + url, sb.toString(), new RemoteObjectResponseCallback(callback));
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
			switch(packet.getCommand()) { // command
			case 'R':
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
	
		public void onError(String message) {
			
		}
		
	}

}
