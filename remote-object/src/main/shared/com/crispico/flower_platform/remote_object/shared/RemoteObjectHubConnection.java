package com.crispico.flower_platform.remote_object.shared;

import jsinterop.annotations.JsType;

@JsType(namespace="rapp_mini_server")
public class RemoteObjectHubConnection {

	public static final int HUB_MODE_HTTP_PULL = 0;

	public static final int HUB_MODE_HTTP_PUSH = 1;

	public static final int HUB_MODE_WEB_SOCKET = 2;

	public static final int HUB_MODE_SERIAL = 3;
	

	private IRequestSender requestSender;

	/**
	 * May be null (e.g. for GWT client)
	 */
	private IRemoteObjectServiceInvoker serviceInvoker;
	
	private IScheduler scheduler;

	/**
	 * IP address or hostname of the hub.  
	 */
	private String remoteAddress;
	
	private String securityToken;
	
	/**
	 * Local rapp instance id
	 */
	private String localNodeId;

	private int localServerPort;
	
	private boolean registered;

	private boolean started;

	private int pollInterval = 5000;

	ResponseCallback registrationCallback;
	
	
	public RemoteObjectHubConnection setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
		return this;
	}

	public RemoteObjectHubConnection setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
		return this;
	}

	public RemoteObjectHubConnection setLocalNodeId(String localNodeId) {
		this.localNodeId = localNodeId;
		return this;
	}

	public RemoteObjectHubConnection setRequestSender(IRequestSender requestSender) {
		this.requestSender = requestSender;
		return this;
	}
	
	public RemoteObjectHubConnection setScheduler(IScheduler scheduler) {
		this.scheduler = scheduler;
		return this;
	}

	public RemoteObjectHubConnection setServiceInvoker(IRemoteObjectServiceInvoker serviceInvoker) {
		this.serviceInvoker = serviceInvoker;
		return this;
	}

	public RemoteObjectHubConnection setLocalServerPort(int localServerPort) {
		this.localServerPort = localServerPort;
		return this;
	}

	public RemoteObjectHubConnection setPollInterval(int pollInterval) {
		this.pollInterval = pollInterval;
		return this;
	}

	public void start(ResponseCallback registrationCallback) {
		started = true;
		this.registrationCallback = registrationCallback;
		requestRegistration();
	}
	
	public void stop() {
		started = false;
		scheduler.clear();
	}

	private void scheduleConnect() {
		if (started && pollInterval > 0) {
			scheduler.schedule(new HubConnectTask(), pollInterval);
		}
	}

	private void requestRegistration() {
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(securityToken, 'A');
		packet.addField(localNodeId);
		packet.addField("" + localServerPort);
		requestSender.sendRequest("http://" + remoteAddress +"/hub", packet.getRawData(), new HubResponseCallback());
	}

	private void requestPendingInvocations() {
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(securityToken, 'J');
		requestSender.sendRequest("http://" + remoteAddress +"/hub", packet.getRawData(), new HubResponseCallback());
	}

	private void requestPendingResponses() {
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(securityToken, 'S');
		requestSender.sendRequest("http://" + remoteAddress +"/hub", packet.getRawData(), new HubResponseCallback());
	}
	
	public void pollHub() {
		if (!registered) {
			requestRegistration();
		} else if (serviceInvoker != null) {
			requestPendingInvocations();
		} else {
			requestPendingResponses();
		}
	}
	
	class HubConnectTask implements Runnable {
		
		@Override
		public void run() {
			pollHub();
		}
		
	}
	
	class HubResponseCallback implements ResponseCallback {

		@Override
		public void onSuccess(Object response) {
			try {
				FlowerPlatformRemotingProtocolPacket[] packets = FlowerPlatformRemotingProtocolPacket.getPackets(response.toString());
				FlowerPlatformRemotingProtocolPacket responsePacket = packets[0];
	
				String callbackId;
	
				switch (responsePacket.getCommand()) {
				case 'A':
					registered = true;
					if (registrationCallback != null) {
						registrationCallback.onSuccess(RemoteObjectHubConnection.this);
					}
					if (pollInterval <= 0) {
						break;
					}
					if (serviceInvoker != null) {
						requestPendingInvocations();
					} else {
						requestPendingResponses();
					}
					break;
				case 'I': {
					if (serviceInvoker == null) {
						scheduleConnect();
						break;
					}
					responsePacket.nextField(); // nodeId (ignored)
					callbackId = responsePacket.nextField();
					Object value = serviceInvoker.invoke(responsePacket);
					FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(securityToken, 'R');
					packet.addField(callbackId);
					packet.addField(value.toString());
					requestSender.sendRequest("http://" + remoteAddress + "/hub", packet.getRawData(), new HubResponseCallback());
					break; }
				case 'J':
					requestPendingResponses();
					break;
				case 'R': {
					for (FlowerPlatformRemotingProtocolPacket packet : packets) {
						callbackId = packet.nextField();
						String valueStr = packet.nextField();
						ResultCallback callback = RemoteObject.getCallbacks().get(callbackId);
						if (callback != null) {
							callback.run(valueStr);
						}
					}
					scheduleConnect();
					break; }
				default:
					scheduleConnect();
					break;
				}
			} catch (Exception e) {
				scheduleConnect();
			}
		}

		@Override
		public void onError(String message) {
			registered = false;
			scheduleConnect();
		}
		
	}
	
}
