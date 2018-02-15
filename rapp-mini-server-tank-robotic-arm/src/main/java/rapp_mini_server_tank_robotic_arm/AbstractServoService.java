package rapp_mini_server_tank_robotic_arm;

import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentPropertiesCapable;
import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentProperty;

public abstract class AbstractServoService extends PersistentPropertiesCapable {
	
	protected int pin;
	
	@PersistentProperty
	protected int minPosition = 500;
	
	@PersistentProperty
	protected int maxPosition = 2500;
	
	@PersistentProperty
	protected int currentPosition = 1500;

	public AbstractServoService(PersistentPropertiesCapable parent, String referencingPropertyFromParent, int pin) {
		super(parent, referencingPropertyFromParent);
		this.pin = pin;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition, boolean save) throws Exception {
		setServoPosition(currentPosition);
		if (save) {
			this.currentPosition = currentPosition;
			storePersistentProperties();
		}
	}

	protected abstract void setServoPosition(int currentPosition) throws Exception;  
	
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
