package rapp_mini_server_tank_robotic_arm;

import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentPropertiesCapable;
import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentProperty;

import jpigpio.JPigpio;
import jpigpio.PigpioException;

public class ServoService extends PersistentPropertiesCapable {
	
	protected JPigpio pigpio;
	
	protected int pin;
	
	@PersistentProperty
	protected int minPosition = 500;
	
	@PersistentProperty
	protected int maxPosition = 2500;
	
	@PersistentProperty
	protected int currentPosition = 1500;

	public ServoService(PersistentPropertiesCapable parent, String referencingPropertyFromParent, JPigpio pigpio, int pin) {
		super(parent, referencingPropertyFromParent);
		this.pigpio = pigpio;
		this.pin = pin;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition, boolean save) throws PigpioException {
		pigpio.gpioServo(pin, currentPosition);
		if (save) {
			this.currentPosition = currentPosition;
			storePersistentProperties();
		}
	}

	public int getMinPosition() {
		return minPosition;
	}

	public void setMinPosition(int minPosition) {
		this.minPosition = minPosition;
	}

	public int getMaxPosition() {
		return maxPosition;
	}

	public void setMaxPosition(int maxPosition) {
		this.maxPosition = maxPosition;
	}

}
