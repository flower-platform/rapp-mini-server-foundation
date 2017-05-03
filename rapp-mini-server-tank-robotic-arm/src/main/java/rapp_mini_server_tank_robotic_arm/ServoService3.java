package rapp_mini_server_tank_robotic_arm;

import jpigpio.JPigpio;
import jpigpio.PigpioException;

public class ServoService3 {

	protected JPigpio pigpio;
	
	protected int pin;

	public ServoService3(JPigpio pigpio, int pin) {
		super();
		this.pigpio = pigpio;
		this.pin = pin;
	}

	// TODO CS: am lasat tip de retur ca da exceptie daca e void
	public String setPosition(int value) {
		try {
			pigpio.gpioServo(pin, value);
		} catch (PigpioException e) {
			throw new RuntimeException(e);
		}
		return "";
	}
	
}
