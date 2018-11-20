package rapp_mini_server_sample1;
import com.crispico.flower_platform.remote_object.RemoteObjectServiceInvoker;
import com.flowerplatform.rapp_mini_server.AbstractRappMiniServerMain;

/**
 * @author Cristian Spiescu
 */
public class RappMiniServerSample1MainGen extends AbstractRappMiniServerMain {

	public HelloService helloService = new HelloService();

	public SampleJavaAppService javaAppService = new SampleJavaAppService();
	
	public static void main(String... args) throws Exception {
		RappMiniServerSample1Main main = new RappMiniServerSample1Main();
		RemoteObjectServiceInvoker.getInstance().setServiceInstance(main.javaAppService);
		main.port = 9001;
		main.run();
	}
	
}
