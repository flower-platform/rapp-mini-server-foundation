package com.flowerplatform.rapp_mini_server.shared;

import jsinterop.annotations.JsType;

@JsType(namespace="rapp_mini_server")
public class RemoteObjectHubClient {

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
	private String localRappInstanceId;

	private boolean registered;
	
	public RemoteObjectHubClient setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
		return this;
	}

	public RemoteObjectHubClient setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
		return this;
	}

	public RemoteObjectHubClient setLocalRappInstanceId(String rappInstanceId) {
		this.localRappInstanceId = rappInstanceId;
		return this;
	}

	public RemoteObjectHubClient setRequestSender(IRequestSender requestSender) {
		this.requestSender = requestSender;
		return this;
	}
	
	public RemoteObjectHubClient setScheduler(IScheduler scheduler) {
		this.scheduler = scheduler;
		return this;
	}

	public void start() {
		requestRegistration();
	}
	
	private void requestRegistration() {
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(securityToken, 'A');
		packet.addField(localRappInstanceId);
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
			case 'I':
				if (serviceInvoker != null) {
					break;
				}
				responsePacket.nextField(); // hasNext (ignored)
				responsePacket.nextField(); // rappInstanceId (ignored)
				callbackId = responsePacket.nextField();
				try {
					Object value = serviceInvoker.invoke(responsePacket);
					FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(securityToken, 'R');
					packet.addField(callbackId);
					packet.addField(value.toString());
					requestSender.sendRequest("http://" + remoteAddress + "/hub", packet.getRawData(), new HubResponseCallback());
				} catch (Exception e) {
					scheduler.schedule(new HubConnectTask(), 5000);
				}
				break;
			case 'J':
				requestPendingResponses();
				break;
			case 'R':
				for (FlowerPlatformRemotingProtocolPacket packet : packets) {
					packet.nextField(); // hasNext (ignored)
					callbackId = packet.nextField();
					String valueStr = packet.nextField();
					ResultCallback callback = RemoteObject.getCallbacks().get(callbackId);
					if (callback != null) {
						callback.run(valueStr);
					}
				}
				scheduler.schedule(new HubConnectTask(), 5000);
				break;
			default:
				scheduler.schedule(new HubConnectTask(), 5000);
			}
		}

		@Override
		public void onError() {
			registered = false;
			scheduler.schedule(new HubConnectTask(), 5000);
		}
		
	}
	
}
