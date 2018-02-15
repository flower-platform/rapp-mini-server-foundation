package rapp_mini_server_tank_robotic_arm.obsolete;

import com.flowerplatform.rapp_mini_server.python.PythonRemoteControl;

public class ServoService_PythonRemoteControl {

	protected PythonRemoteControl pythonRemoteControl;
	
	protected String instance;
	
	protected int pin;

	public ServoService_PythonRemoteControl(PythonRemoteControl pythonRemoteControl, String instance, int pin) {
		super();
		this.pythonRemoteControl = pythonRemoteControl;
		this.instance = instance;
		this.pin = pin;
	}

	// TODO CS: am lasat tip de retur ca da exceptie daca e void
	public String setPosition(int value) {
		pythonRemoteControl.callFunction(instance, "set_servo", "int", Integer.toString(pin), "int", Integer.toString(value));
		return "";
	}
	
}
