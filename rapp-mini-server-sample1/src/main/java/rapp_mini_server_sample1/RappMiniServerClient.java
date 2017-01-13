package rapp_mini_server_sample1;

import com.flowerplatform.rapp_mini_server.remote_object.JavaRemoteObjectBase;
import com.flowerplatform.rapp_mini_server.remote_object.RemoteObjectServiceInvoker;
import com.flowerplatform.rapp_mini_server.shared.RemoteObject;
import com.flowerplatform.rapp_mini_server.shared.RemoteObjectHubConnection;
import com.flowerplatform.rapp_mini_server.shared.ResultCallback;

public class RappMiniServerClient {

	JavaRemoteObjectBase remoteObjectBase = new JavaRemoteObjectBase();

	public void startHubClient()  {
		RemoteObjectHubConnection client = new RemoteObjectHubConnection()
				.setLocalRappInstanceId("JavaHubClient")
				.setRemoteAddress("localhost:9001")
				.setSecurityToken("55555555")
				.setRequestSender(remoteObjectBase)
				.setScheduler(remoteObjectBase)
				.setServiceInvoker(new RemoteObjectServiceInvoker(new RappMiniServerSample1Main()));
		client.start();
	}
	
	public void callArduinoDirect() {
		RemoteObject ro = new RemoteObject()
				.setSecurityToken("12345678")
				.setRemoteAddress("192.168.100.251:9001")
				.setInstanceName("lightController1")
				.setRequestSender(remoteObjectBase);

		ro.invokeMethod("sayHello", new Object[] {"ArduinoDirect", 3 }, new ResultCallback() {
			@Override
			public void run(Object result) {
				System.out.println("Arudino direct: " + result);
			}
		});

	}
	
	public void callArduinoHub() {
		RemoteObject ro = new RemoteObject()
				.setSecurityToken("55555555")
				.setRemoteAddress("localhost:9001")
				.setRappInstanceId("baieRapp")
				.setInstanceName("lightController1")
				.setRequestSender(remoteObjectBase);

		ro.invokeMethod("sayHello", new Object[] {"ArduinoHub", 3 }, new ResultCallback() {
			@Override
			public void run(Object result) {
				System.out.println("Arduino hub: " + result);
			}
		});
		
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
		});

	}

	public void callJavaHub() {
		RemoteObject ro = new RemoteObject()
				.setSecurityToken("55555555")
				.setRemoteAddress("localhost:9001")
				.setRappInstanceId("JavaHubClient")
				.setInstanceName("helloService")
				.setRequestSender(remoteObjectBase);

		ro.invokeMethod("sayHello", new Object[] {"JavaHub", 3 }, new ResultCallback() {
			@Override
			public void run(Object result) {
				System.out.println("Java hub: " + result);
			}
		});
	}
	
	public void test() {
		startHubClient();
		callArduinoDirect();
		callArduinoHub();
//		callArduinoHub();
//		callArduinoHub();
		callJavaDirect();
		callJavaHub();
	}
	
	public static void main(String[] args) {
		new RappMiniServerClient().test();
	}
	
}
