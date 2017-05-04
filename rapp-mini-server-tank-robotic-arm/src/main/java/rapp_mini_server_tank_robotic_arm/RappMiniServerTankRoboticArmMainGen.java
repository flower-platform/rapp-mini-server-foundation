package rapp_mini_server_tank_robotic_arm;

import com.flowerplatform.rapp_mini_server.AbstractRappMiniServerMain;
import com.flowerplatform.rapp_mini_server.logback.LogbackConfigurator;
import com.flowerplatform.rapp_mini_server.persistent_properties.PersistentPropertiesCapable;
import com.flowerplatform.rapp_mini_server.remote_object.RemoteObjectServiceInvoker;

import jpigpio.JPigpio;
import jpigpio.PigpioSocket;

public class RappMiniServerTankRoboticArmMainGen extends AbstractRappMiniServerMain {

//	public ServoService servo1;
//	public ServoService servo2;

//	public ServoService2 servo1;
//	public ServoService2 servo2;

//	public ServoService_JPigpio servo1;
//	public ServoService_JPigpio servo2;
//	public ServoService_JPigpio servo3;
//	
//	public RoboticArmAndTankService roboticArmAndTankService;
	
	public ServoService servoService1;
	public ServoService servoService2;
	public ServoService servoService3;
	public ServoService servoService4;
	public ServoService servoService5;
	public ServoService servoService6;
	
	public static void main(String[] args) throws Exception {
		System.setProperty(LogbackConfigurator.LOGBACK_MAIN_XML, "true");
		
		System.out.println("Salut ba4!");
		
//		prc.callFunction("servo", "set_servo", "int", "17", "int", "1500");
//		Thread.sleep(2000);
//		prc.callFunction("servo", "set_servo", "int", "17", "int", "1830");
//		Thread.sleep(2000);
		
//		System.out.println("Waiting for commands...");
//		
		RappMiniServerTankRoboticArmMainGen main = new RappMiniServerTankRoboticArmMainGen();
		main.port = 9001;
		main.serviceInvoker = new RemoteObjectServiceInvoker(main);

//		PythonRemoteControl prc = new PythonRemoteControl();
//		prc.startPythonProcess();
//		prc.createObject("servo1", "RPIO.PWM.Servo");
//		prc.createObject("servo2", "RPIO.PWM.Servo");
//		main.servo1 = new ServoService(prc, "servo1", 26);
//		main.servo2 = new ServoService(prc, "servo2", 19);
		
//		main.servo1 = new ServoService2(26);
//		main.servo2 = new ServoService2(18);
		
//		SleepUtil.sleepMillis(1000);
//		main.servo2.setPosition(1800);
//		SleepUtil.sleepMillis(1000);
//		main.servo2.setPosition(1400);
//		SleepUtil.sleepMillis(1000);

//		{
//	        Server server = new Server(main.port);
//	        ServletHandler handler = new ServletHandler();
//	        server.setHandler(handler);
//	        populateServletHandler(handler);
//	        server.start();
//	
//	        // The use of server.join() the will make the current thread join and
//	        // wait until the server is done executing.
//	        // See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
//	        if (threadJoin) {
//	            server.join();
//	        }
//		}
		
		PersistentPropertiesCapable.setPropertiesFilePath("../persistent.properties");

		JPigpio pigpio = null;
		pigpio = new PigpioSocket("localhost", 8888);
//		main.roboticArmAndTankService = new RoboticArmAndTankService();

//		main.servo1 = new ServoService_JPigpio(pigpio, 13);
//		main.roboticArmAndTankService.servos.put("Servo1", main.servo1);
//		main.servo2 = new ServoService_JPigpio(pigpio, 19);
//		main.roboticArmAndTankService.servos.put("Servo2", main.servo2);
//		main.servo3 = new ServoService_JPigpio(pigpio, 26);
//		main.roboticArmAndTankService.servos.put("Servo3", main.servo3);
		
//		main.roboticArmAndTankService.servos.put("Servo4", new ServoService_JPigpio(pigpio, 16));
//		main.roboticArmAndTankService.servos.put("Servo5", new ServoService_JPigpio(pigpio, 20));
//		main.roboticArmAndTankService.servos.put("Servo6", new ServoService_JPigpio(pigpio, 21));
		
		// TODO CS: ce facem cu necesitatea acestui load? sa-l transormam in init? sau vreun "start"?
		main.servoService1 = new ServoService(null, "servoService1", pigpio, 13).loadPersistentProperties();
		main.servoService2 = new ServoService(null, "servoService2", pigpio, 19).loadPersistentProperties();
		main.servoService3 = new ServoService(null, "servoService3", pigpio, 26).loadPersistentProperties();
		main.servoService4 = new ServoService(null, "servoService4", pigpio, 16).loadPersistentProperties();
		main.servoService5 = new ServoService(null, "servoService5", pigpio, 20).loadPersistentProperties();
		main.servoService6 = new ServoService(null, "servoService6", pigpio, 21).loadPersistentProperties();
		
//		new Thread() {
//			@Override
//			public void run() {
//				Test_ServoSweep.main(null);
//			}
//		}.start();
		
		main.run();
//		System.out.println("cucu");
		
//		LEDTest.test(21);
//		ServoTest.test(50, 26);
	}

}
