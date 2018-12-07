package rapp_mini_server_sample1;

import static com.crispico.flower_platform.remote_object.shared.RemoteObjectHubConnection.HUB_MODE_HTTP_PUSH;

import java.util.LinkedHashMap;
import java.util.Map;

import com.crispico.flower_platform.remote_object.JavaRemoteObjectBase;
import com.crispico.flower_platform.remote_object.RemoteObjectServiceInvoker;
import com.crispico.flower_platform.remote_object.shared.RemoteObject;
import com.crispico.flower_platform.remote_object.shared.RemoteObjectHubConnection;

public class SampleJavaAppService {

	private Map<String, String> testResults = new LinkedHashMap<>();
	
	private JavaRemoteObjectBase remoteObjectBase = new JavaRemoteObjectBase();

	private RemoteObject ro1;

	private RemoteObject ro2;
	
	private RemoteObjectHubConnection hubConnection;
	
	private Object tmpResult;
	
	public String sayHello(String name, int n, float f, boolean b) {
		return String.format("Hello from Java, %s! n=%s, f=%s, b=%s", name, n, f, b);
	}
	
	public ComplexObject sayHelloComplex(ComplexObject object) {
		object.setB("A=" + object.getA());
		return object;
	}
	
	
	public void initRemoteObject1(String ip, int port, String objectName, String securityToken) {
		ro1 = new RemoteObject().setRemoteAddress(ip + ":" + port).setSecurityToken(securityToken).setInstanceName(objectName);
		ro1.setRequestSender(remoteObjectBase);
	}

	public void initRemoteObject2(String ip, int port, String objectName, String securityToken, String nodeId) {
		ro2 = new RemoteObject().setRemoteAddress(ip + ":" + port).setSecurityToken(securityToken).setInstanceName(objectName).setNodeId(nodeId);
		ro2.setRequestSender(remoteObjectBase);
	}

	private String callSayHelloRo(String name, RemoteObject remoteObject) {
		tmpResult = null;
		remoteObject.invokeMethod(
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

	public String callSayHelloRo1(String name) {
		return callSayHelloRo(name, ro1);
	}
	
	public String callSayHelloRo2(String name) {
		return callSayHelloRo(name, ro2);
	}

	
	public void connectToHub(String hubIp, int port, String nodeId, String securityToken, int mode, int pollingInterval) {
		disconnectFromHub();
		
		hubConnection = new RemoteObjectHubConnection()
				.setRemoteAddress(hubIp + ":" + port)
				.setLocalNodeId(nodeId)
				.setSecurityToken(securityToken)
				.setPollInterval(pollingInterval)
				.setRequestSender(remoteObjectBase).setScheduler(remoteObjectBase).setServiceInvoker(RemoteObjectServiceInvoker.getInstance());
		
		if (mode == HUB_MODE_HTTP_PUSH) {
			hubConnection.setLocalServerPort(9001); // could be obtained from main file of rapp (i.e. RappMiniServerSample1Main)
		}
		hubConnection.start(null);
	}

	public void disconnectFromHub() {
		if (hubConnection != null) {
			hubConnection.stop();
		}
	}

	public void pollNowHub() {
		hubConnection.pollHub();
	}

	public Map<String, String> getTestResults() {
		return testResults;
	}

	public void saveTestResult(String testName, String result) {
		System.out.println(String.format("TEST RESULT RECEIVED: %s %s", testName, result));
		testResults.put(testName, result);
	}

}
