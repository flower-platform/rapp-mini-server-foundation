package rapp_mini_server_sample1;

/**
 * @author Cristian Spiescu
 */
public class ComplexObject {

	private int a;
	
	private String b;

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public String getB() {
		return b;
	}

	public void setB(String b) {
		this.b = b;
	}

	public ComplexObject() {
		super();
	}

	public ComplexObject(int a, String b) {
		super();
		this.a = a;
		this.b = b;
	}

	@Override
	public String toString() {
		return "ComplexObject [a=" + a + ", b=" + b + "]";
	}
	
}
