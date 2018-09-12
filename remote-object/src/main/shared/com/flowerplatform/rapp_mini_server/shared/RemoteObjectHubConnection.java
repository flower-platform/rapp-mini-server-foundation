package com.flowerplatform.rapp_mini_server.shared;

import jsinterop.annotations.JsType;

@JsType(namespace="rapp_mini_server")
public class RemoteObjectHubConnection {

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

	public void start() {
		requestRegistration();
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
	
	class HubConnectTask implements Runnable {
		
		@Override
		public void run() {
			if (!registered) {
				requestRegistration();
			} else if (serviceInvoker != null) {
				requestPendingInvocations();
			} else {
				requestPendingResponses();
			}
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
					if (serviceInvoker != null) {
						requestPendingInvocations();
					} else {
						requestPendingResponses();
					}
					break;
				case 'I': {
					if (serviceInvoker == null) {
						scheduler.schedule(new HubConnectTask(), 5000);
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
					scheduler.schedule(new HubConnectTask(), 5000);
					break; }
				default:
					scheduler.schedule(new HubConnectTask(), 5000);
				}
			} catch (Exception e) {
				scheduler.schedule(new HubConnectTask(), 5000);
			}
		}

		@Override
		public void onError(String message) {
			registered = false;
			scheduler.schedule(new HubConnectTask(), 5000);
		}
		
	}
	
}
