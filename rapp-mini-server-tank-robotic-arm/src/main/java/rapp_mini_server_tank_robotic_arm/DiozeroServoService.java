package rapp_mini_server_tank_robotic_arm;

import com.diozero.devices.Servo;
import com.diozero.devices.Servo.Trim;
import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentPropertiesCapable;

public class DiozeroServoService extends AbstractServoService {
	
	protected Servo servo;

	public DiozeroServoService(PersistentPropertiesCapable parent, String referencingPropertyFromParent, int pin) {
		super(parent, referencingPropertyFromParent, pin);
		servo = new Servo(pin, 1.5f, Trim.MG996R);
	}

	@Override
	protected void setServoPosition(int currentPosition) {
		servo.setValue(currentPosition / 1000f);
		System.out.println(currentPosition / 1000f + " " + servo.getValue() + " " + servo.getPulseWidthMs() + " " + servo.getAngle());
	}

}
