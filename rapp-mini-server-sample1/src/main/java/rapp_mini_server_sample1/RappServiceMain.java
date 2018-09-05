package rapp_mini_server_sample1;

import com.flowerplatform.rapp_mini_server.AbstractRappMiniServerMain;
import com.flowerplatform.rapp_mini_server.remote_object.RemoteObjectServiceInvoker;

public class RappServiceMain extends AbstractRappMiniServerMain {

	public RappService rappService = new RappService();

	public static void main(String... args) throws Exception {
		RappServiceMain main = new RappServiceMain();
		main.port = 9001;
		main.serviceInvoker = new RemoteObjectServiceInvoker(main);
		main.run();
	}
	
}
