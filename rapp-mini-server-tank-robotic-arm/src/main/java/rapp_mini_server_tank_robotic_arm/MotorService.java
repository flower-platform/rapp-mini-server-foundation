package rapp_mini_server_tank_robotic_arm;

import com.diozero.api.motor.PwmMotor;
import com.diozero.util.RuntimeIOException;

public class MotorService {

	private PwmMotor motor;
	
	public MotorService(int forwardPin, int backwardPin) {
		motor = new PwmMotor(forwardPin, backwardPin);
	}

	public void backward(int speed) throws RuntimeIOException {
		motor.backward(speed / 10f);
	}

	public void forward(int speed) throws RuntimeIOException {
		motor.forward(speed / 10f);
	}

	public void stop() throws RuntimeIOException {
		motor.stop();
	}
	
}
