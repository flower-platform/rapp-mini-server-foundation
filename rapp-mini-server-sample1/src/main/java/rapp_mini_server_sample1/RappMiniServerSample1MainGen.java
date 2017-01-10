package rapp_mini_server_sample1;
import com.flowerplatform.rapp_mini_server.AbstractRappMiniServerMain;
import com.flowerplatform.rapp_mini_server.remote_object.RemoteObjectServiceInvoker;

/**
 * @author Cristian Spiescu
 */
public class RappMiniServerSample1MainGen extends AbstractRappMiniServerMain {

	public HelloService helloService = new HelloService();
	
	public static void main(String... args) throws Exception {
		RappMiniServerSample1Main main = new RappMiniServerSample1Main();
		main.port = 9001;
		
		main.serviceInvoker = new RemoteObjectServiceInvoker(main);
		
		main.run();
	}
	
}
