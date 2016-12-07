package rapp_mini_server_sample1;

/**
 * @author Cristian Spiescu
 */
public class HelloService extends HelloServiceGen {

	@Override
	public String sayHello(String name, int times) throws Exception {
		return super.sayHello(name, times) + "+ some custom code!";
	}

}
