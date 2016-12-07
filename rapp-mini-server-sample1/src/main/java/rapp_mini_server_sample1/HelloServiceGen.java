package rapp_mini_server_sample1;

/**
 * @author Cristian Spiescu
 */
public class HelloServiceGen {
	
	public String sayHello(String name, int times) throws Exception {
		String result = "Hello ";
		for (int i = 0; i < times; i++) {
			result += name + " ";
		}
		return result;
	}
}
