package rapp_mini_server_sample1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoboticArmAndTankService extends RoboticArmAndTankServiceGen {

	Map<String, Integer> servoPositionsMap = new HashMap<>();
	Map<String, List<Integer>> servoLimitsMap = new HashMap<>();
	Map<String, Integer> caterpillarSpeedMap = new HashMap<>();
	
	public Integer getServoPosition(String servoId) {
		if (!servoPositionsMap.containsKey(servoId)) {
			servoPositionsMap.put(servoId, 40);
		}
		return servoPositionsMap.get(servoId);
	}
	
	public void setServoPosition(String servoId, Integer position) {
		servoPositionsMap.put(servoId, position);
	}
	
	/**
	 * @return a list containing on the first position the minimum limit 
	 * and on the second position the maximum limit
	 */
	public List<Integer> getServoLimits(String servoId) {
		if (!servoLimitsMap.containsKey(servoId)) {
			servoLimitsMap.put(servoId, Arrays.asList(0, 100));
		}
		
		return servoLimitsMap.get(servoId);
	}
	
	public void setServoLimits(String servoId, Integer min, Integer max) {
		servoLimitsMap.put(servoId, Arrays.asList(min, max));
	}
	
	public Integer getCaterpillarSpeed(String caterpillarId) {
		if (!caterpillarSpeedMap.containsKey(caterpillarId)) {
			caterpillarSpeedMap.put(caterpillarId, 0);
		}
		return caterpillarSpeedMap.get(caterpillarId);
	}
	
	public void setCaterpillarSpeed(String caterpillarId, Integer speed) {
		caterpillarSpeedMap.put(caterpillarId, speed); 
	}
}
