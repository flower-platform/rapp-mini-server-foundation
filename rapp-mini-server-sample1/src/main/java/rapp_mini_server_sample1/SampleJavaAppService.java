package rapp_mini_server_sample1;

import com.crispico.flower_platform.remote_object.JavaRemoteObjectBase;
import com.crispico.flower_platform.remote_object.shared.RemoteObject;
import com.crispico.flower_platform.remote_object.shared.RemoteObjectHubConnection;

public class SampleJavaAppService {

	private static final int HUB_MODE_PULL = 0;

	private static final int HUB_MODE_PUSH = 1;
	
	private JavaRemoteObjectBase remoteObjectBase = new JavaRemoteObjectBase();

	private RemoteObject directRemoteObject;
	
	private RemoteObjectHubConnection hubConnection;
	
	private RemoteObject hubRemoteObject;
	
	private Object tmpResult;
	
	public String sayHello(String name, int n, float f, boolean b) {
		return String.format("Hello from Java, %s! n=%s, f=%s, b=%s", name, n, f, b);
	}
	
	public ComplexObject sayHelloComplex(ComplexObject object) {
		object.setB("A=" + object.getA());
		return object;
	}
	
	public void initRemoteObjectDirect(String ip, int port, String objectName, String securityToken) {
		directRemoteObject = new RemoteObject().setRemoteAddress(ip + ":" + port).setSecurityToken(securityToken).setInstanceName(objectName);
		directRemoteObject.setRequestSender(remoteObjectBase);
	}

	public String callSayHelloRoDirect(String name) {
		tmpResult = null;
		directRemoteObject.invokeMethod(
				"sayHello",
				new Object[] { name, 2, 5.23f, true }, 
				(result) -> { 
					System.out.println("Result: " + result); 
					synchronized(SampleJavaAppService.this) {
						tmpResult = result; 
						SampleJavaAppService.this.notifyAll(); 
					}
				}, 
				(error) -> { 
					System.out.println("Error: " + error); 
					synchronized(SampleJavaAppService.this) {
						tmpResult = "ERROR: " + error; 
						SampleJavaAppService.this.notifyAll(); 
					}
				}
		);
		synchronized(this) {
			if (tmpResult == null) {
				try { this.wait(5000); } catch (Exception e) { e.printStackTrace(); } // make it a synchronous call
			}
		}
		return "" + tmpResult;
	}
	
	public void initRemoteObjectViaHub(int mode, String hubIp, int port, String destinationNodeId, int pollingInterval) {
		hubConnection = new RemoteObjectHubConnection()
				.setLocalNodeId("SampleJavaApp")
				.setRemoteAddress(hubIp + ":" + port)
				.setRequestSender(remoteObjectBase).setScheduler(remoteObjectBase);
		
		if (mode == HUB_MODE_PUSH) {
			hubConnection.setLocalServerPort(9001); // could be obtained from main file of rapp (i.e. RappMiniServerSample1Main)
		}
		hubRemoteObject = new RemoteObject().setRemoteAddress(hubIp + ":" + port).setNodeId(destinationNodeId).setSecurityToken("12345678");
		hubConnection.start();
	}

	public void disconnectRemoteObjectViaHub() {
		hubConnection.stop();
	}

	public void pollNowHub() {
		hubConnection.pollHub();
	}

}
