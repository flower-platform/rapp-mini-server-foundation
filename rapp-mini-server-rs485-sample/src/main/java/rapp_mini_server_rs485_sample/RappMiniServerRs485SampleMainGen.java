package rapp_mini_server_rs485_sample;
import com.crispico.flower_platform.remote_object.RemoteObjectServiceInvoker;
import com.crispico.flower_platform.remote_object.SerialBusMasterServlet;
import com.flowerplatform.rapp_mini_server.AbstractRappMiniServerMain;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;

/**
 * @author Cristian Spiescu
 */
public class RappMiniServerRs485SampleMainGen extends AbstractRappMiniServerMain {

	
	public static void main(String... args) throws Exception {
		System.out.println("JAVA LIBRARY PATH: " + System.getProperty("java.library.path"));
		RappMiniServerRs485SampleMain main = new RappMiniServerRs485SampleMain();
		main.port = 9001;
		
		RemoteObjectServiceInvoker.getInstance().setServiceInstance(main);

		main.run();
	}

	@Override
	protected void configureDeploymentInfo(DeploymentInfo deploymentInfo) {
		super.configureDeploymentInfo(deploymentInfo);
		deploymentInfo.addServlets(Servlets.servlet(
				SerialBusMasterServlet.class.getName(), SerialBusMasterServlet.class)
						.addMapping("/serialBus")
						.addInitParam("serialPortName", "/dev/ttyS0")
						.addInitParam("baudRate", "115200")
						.addInitParam("writeEnablePin", "18")
						.addInitParam("timeoutMillis", "2000")
		);
	}
	
}
