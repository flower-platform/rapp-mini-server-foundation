package rapp_mini_server_sample1;

import com.crispico.flower_platform.remote_object.JavaRemoteObjectBase;
import com.crispico.flower_platform.remote_object.RemoteObjectServiceInvoker;
import com.crispico.flower_platform.remote_object.shared.RemoteObject;
import com.crispico.flower_platform.remote_object.shared.RemoteObjectHubConnection;
import com.crispico.flower_platform.remote_object.shared.ResultCallback;

public class RappMiniServerClient {

	JavaRemoteObjectBase remoteObjectBase = new JavaRemoteObjectBase();

	public void startHubClient()  {
		RemoteObjectHubConnection client = new RemoteObjectHubConnection()
				.setLocalNodeId("JavaHubClient")
				.setRemoteAddress("localhost:9001")
				.setSecurityToken("55555555")
				.setRequestSender(remoteObjectBase)
				.setScheduler(remoteObjectBase)
				.setServiceInvoker(RemoteObjectServiceInvoker.getInstance());
		client.start();
	}
	
	public void callArduinoDirect() {
		RemoteObject ro = new RemoteObject()
				.setSecurityToken("12345678")
				.setRemoteAddress("192.168.100.251:9001")
				.setInstanceName("lightController1")
				.setRequestSender(remoteObjectBase);

		ro.invokeMethod("sayHello", new Object[] {"ArduinoDirect", 3 }, (result)-> {
				System.out.println("Arudino direct: " + result);
			}, (error) -> {
				System.out.println("Arudino direct ERROR: " + error);
			}
		);

	}
	
	public void callArduinoHub() {
		RemoteObject ro = new RemoteObject()
				.setSecurityToken("55555555")
				.setRemoteAddress("localhost:9001")
				.setNodeId("baieRapp")
				.setInstanceName("lightController1")
				.setRequestSender(remoteObjectBase);

		ro.invokeMethod("sayHello", new Object[] {"ArduinoHub", 3 }, new ResultCallback() {
			@Override
			public void run(Object result) {
				System.out.println("Arduino hub: " + result);
			}
		}, null);
		
	}

	public void callJavaDirect() {
		RemoteObject ro = new RemoteObject()
				.setSecurityToken("00000000")
				.setRemoteAddress("localhost:9001")
				.setInstanceName("helloService")
				.setRequestSender(remoteObjectBase);

		ro.invokeMethod("sayHello", new Object[] {"World", 3 }, new ResultCallback() {
			@Override
			public void run(Object result) {
				System.out.println("Java direct: " + result);
			}
		}, null);

	}

	public void callJavaHub() {
		RemoteObject ro = new RemoteObject()
				.setSecurityToken("55555555")
				.setRemoteAddress("localhost:9001")
				.setNodeId("JavaHubClient")
				.setInstanceName("helloService")
				.setRequestSender(remoteObjectBase);

		ro.invokeMethod("sayHello", new Object[] {"JavaHub", 3 }, new ResultCallback() {
			@Override
			public void run(Object result) {
				System.out.println("Java hub: " + result);
			}
		}, null);
	}
	
	public void callWebSocketUI() {
		RemoteObject ro = new RemoteObject()
				.setSecurityToken("55555555")
				.setRemoteAddress("localhost:9001")
				.setNodeId("robot-ui")
				.setInstanceName("robotUiService")
				.setRequestSender(remoteObjectBase);

		ro.invokeMethod("updateLocation", new Object[] {"sufragerie"}, new ResultCallback() {
			@Override
			public void run(Object result) {
				System.out.println("callWebSocketUI: " + result);
			}
		}, null);
	}
	
	public void test() {
//		startHubClient();
//		callArduinoDirect();
//		callArduinoHub();
//		callArduinoHub();
//		callArduinoHub();
//		callJavaDirect();
//		callJavaHub();
		callWebSocketUI();
	}
	
	public static void main(String[] args) throws Exception {
		new RappMiniServerClient().test();
	}
	
}
