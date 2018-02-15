package rapp_mini_server_tank_robotic_arm;

import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentPropertiesCapable;

import jpigpio.JPigpio;
import jpigpio.PigpioException;

public class JPigpioServoService extends AbstractServoService {

	protected JPigpio pigpio;
	
	public JPigpioServoService(PersistentPropertiesCapable parent, String referencingPropertyFromParent, JPigpio pigpio, int pin) {
		super(parent, referencingPropertyFromParent, pin);
		this.pigpio = pigpio;
	}

	@Override
	protected void setServoPosition(int currentPosition) throws PigpioException {
		pigpio.gpioServo(pin, currentPosition);
	}
	
}
