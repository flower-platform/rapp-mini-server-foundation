package rapp_mini_server_tank_robotic_arm;

import com.diozero.sandpit.Servo;

public class ServoService2 {

	protected Servo servo;
	
	public ServoService2(int pin) {
		super();
		servo = new Servo(pin, 50, 1.5f);
	}

	// TODO CS: am lasat tip de retur ca da exceptie daca e void
	public String setPosition(int value) {
		servo.setPulseWidthMs(((float) value) / 1000);
		return "";
	}
	
}
