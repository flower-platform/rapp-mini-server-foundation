package rapp_mini_server_sample1;

/**
 * @author Cristian Spiescu
 */
public class HelloService extends HelloServiceGen {

	@Override
	public String sayHello(String name, int times) throws Exception {
		return super.sayHello(name, times) + "+ some custom code!";
	}
	
	public ComplexObject sayHelloComplex(String name, int times) {
		return new ComplexObject(times, name);
	}
	
	public ComplexObject sayHelloComplex2(ComplexObject c) {
		System.out.println("Received: " + c);
		return c;
	}

	public int turnOn() throws Exception {
		System.out.println("TURN ON");
		return 3;
	}
	
}
