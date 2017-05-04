package rapp_mini_server_tank_robotic_arm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoboticArmAndTankService {

	protected static final String PERSISTENCE_FILE = "../tank-persistent-data.ser";
	public static final int SERVO_MIN = 500;
	public static final int SERVO_MAX = 2500;
	public static final int SERVO_DEFAULT = 1500;
	
	protected Data data;
	
	protected Map<String, ServoService_JPigpio> servos = new HashMap<>();
	
	protected void save() {
		File file = new File(PERSISTENCE_FILE);
		try (FileOutputStream f = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(f)) {
			oos.writeObject(data);
			System.out.println("Persisting data in: " + file.getAbsolutePath() + " ...");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public RoboticArmAndTankService() {
		super();
		File file = new File(PERSISTENCE_FILE);
		System.out.println("Trying to load data from: '" + file.getAbsolutePath() + "'");
		try (FileInputStream f = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(f) ) {
			data = (Data) ois.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("Data not found; creating a fresh one...");
			data = new Data();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Integer getServoPosition(String servoId) {
		if (!data.servoPositionsMap.containsKey(servoId)) {
			data.servoPositionsMap.put(servoId, SERVO_DEFAULT);
		}
		return data.servoPositionsMap.get(servoId);
	}
	
	public void setServoPosition(String servoId, Integer position) {
		data.servoPositionsMap.put(servoId, position);
		save();
		
		servos.get(servoId).setPosition(position);
	}
	
	public void setServoPositionNoSave(String servoId, Integer position) {
		servos.get(servoId).setPosition(position);
	}
	
	/**
	 * @return a list containing on the first position the minimum limit 
	 * and on the second position the maximum limit
	 */
	public List<Integer> getServoLimits(String servoId) {
		if (!data.servoLimitsMap.containsKey(servoId)) {
			data.servoLimitsMap.put(servoId, Arrays.asList(SERVO_MIN, SERVO_MAX));
		}
		
		return data.servoLimitsMap.get(servoId);
	}
	
	public void setServoLimits(String servoId, Integer min, Integer max) {
		data.servoLimitsMap.put(servoId, Arrays.asList(min, max));
		save();
	}
	
	public Integer getCaterpillarSpeed(String caterpillarId) {
		if (!data.caterpillarSpeedMap.containsKey(caterpillarId)) {
			data.caterpillarSpeedMap.put(caterpillarId, 0);
		}
		return data.caterpillarSpeedMap.get(caterpillarId);
	}
	
	public void setCaterpillarSpeed(String caterpillarId, Integer speed) {
		data.caterpillarSpeedMap.put(caterpillarId, speed); 
	}
}
