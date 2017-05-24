package rapp_mini_server_tank_robotic_arm;

import com.diozero.api.PwmOutputDevice;
import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentPropertiesCapable;

public class DioZeroPwmOutputServoService extends AbstractServoService {
	
	protected PwmOutputDevice pwmOut;

	public DioZeroPwmOutputServoService(PersistentPropertiesCapable parent, String referencingPropertyFromParent, int pin) {
		super(parent, referencingPropertyFromParent, pin);
		pwmOut = new PwmOutputDevice(pin, 50, currentPosition / 1000f / 20f);
	}

	@Override
	protected void setServoPosition(int currentPosition) {
		pwmOut.setValue(currentPosition / 1000f / 20f);
	}

}
