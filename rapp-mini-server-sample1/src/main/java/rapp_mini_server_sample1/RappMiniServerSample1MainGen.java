package rapp_mini_server_sample1;
import com.crispico.flower_platform.remote_object.RemoteObjectHubServlet;
import com.crispico.flower_platform.remote_object.RemoteObjectServiceInvoker;
import com.crispico.flower_platform.remote_object.RemoteObjectServlet;
import com.crispico.flower_platform.remote_object.WebSocketServerEndpoint;
import com.flowerplatform.rapp_mini_server.AbstractRappMiniServerMain;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

/**
 * @author Cristian Spiescu
 */
public class RappMiniServerSample1MainGen extends AbstractRappMiniServerMain {

	public HelloService helloService = new HelloService();

	public SampleJavaAppService javaAppService = new SampleJavaAppService();
	
	@Override
	protected void configureDeploymentInfo(DeploymentInfo deploymentInfo) {
		deploymentInfo
				.addServlets(Servlets.servlet(RemoteObjectServlet.class.getName(), RemoteObjectServlet.class).addMapping("/remoteObject/*").addInitParam("securityToken", "JAVA_TKN"))
				.addServlets(Servlets.servlet(RemoteObjectHubServlet.class.getName(), RemoteObjectHubServlet.class).addMapping("/hub/*").addInitParam("securityToken", "JAVA_TKN"))
				.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, new WebSocketDeploymentInfo().addEndpoint(WebSocketServerEndpoint.class));
	}

	public void start() {
		RemoteObjectServiceInvoker.getInstance().setServiceInstance(javaAppService);
		port = 9001;
		run();
	}
	
	public static void main(String... args) throws Exception {
		RappMiniServerSample1Main main = new RappMiniServerSample1Main();
		main.start();
	}
	
}
