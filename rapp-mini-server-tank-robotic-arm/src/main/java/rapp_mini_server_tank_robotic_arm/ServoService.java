package rapp_mini_server_tank_robotic_arm;

import com.diozero.api.PwmOutputDevice;
import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentPropertiesCapable;
import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentProperty;

import jpigpio.PigpioException;

public class ServoService extends PersistentPropertiesCapable {
	
//	protected Servo servo;
	
	protected PwmOutputDevice pwmOut;
	
	protected int pin;
	
	@PersistentProperty
	protected int minPosition = 0;
	
	@PersistentProperty
	protected int maxPosition = 2500;
	
	@PersistentProperty
	protected int currentPosition = 1500;

	public ServoService(PersistentPropertiesCapable parent, String referencingPropertyFromParent, int pin) {
		super(parent, referencingPropertyFromParent);
		this.pin = pin;
		pwmOut = new PwmOutputDevice(pin, 50, currentPosition / 1000f / 20f);
//		servo = new Servo(pin, 1.5f, Trim.TOWERPRO_SG90);
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition, boolean save) throws PigpioException {
		System.out.println(currentPosition / 1000f / 20f);
		if (pwmOut ==null) {
			pwmOut = new PwmOutputDevice(pin, 50, currentPosition / 1000f / 20f);
		} else {
			pwmOut.setValue(currentPosition / 1000f / 20f);
		}
//		servo.setPulseWidthMs(currentPosition / 1000f);
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
