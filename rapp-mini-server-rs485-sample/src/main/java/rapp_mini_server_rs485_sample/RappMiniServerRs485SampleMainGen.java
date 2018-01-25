package rapp_mini_server_rs485_sample;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.flowerplatform.rapp_mini_server.AbstractRappMiniServerMain;
import com.flowerplatform.rapp_mini_server.remote_object.RemoteObjectServiceInvoker;
import com.flowerplatform.rapp_mini_server.remote_object.SerialBusMasterServlet;

/**
 * @author Cristian Spiescu
 */
public class RappMiniServerRs485SampleMainGen extends AbstractRappMiniServerMain {

	
	public static void main(String... args) throws Exception {
		System.out.println("JAVA LIBRARY PATH: " + System.getProperty("java.library.path"));
		RappMiniServerRs485SampleMain main = new RappMiniServerRs485SampleMain();
		main.port = 9001;
		
		main.serviceInvoker = new RemoteObjectServiceInvoker(main);
		
		main.run();
	}

	@Override
	protected void addRemoteObjectsServlet(ServletHandler handler) {
		super.addRemoteObjectsServlet(handler);
		try {
			SerialBusMasterServlet sbm = new SerialBusMasterServlet("/dev/ttyS0", 115200, 18, 2000);
			handler.addServletWithMapping(new ServletHolder(sbm), "/serialBus");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
