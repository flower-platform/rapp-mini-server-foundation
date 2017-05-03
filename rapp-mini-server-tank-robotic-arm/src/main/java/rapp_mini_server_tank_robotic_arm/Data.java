package rapp_mini_server_tank_robotic_arm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data implements Serializable {
	private static final long serialVersionUID = -8762458360204478231L;
	Map<String, Integer> servoPositionsMap = new HashMap<>();
	Map<String, List<Integer>> servoLimitsMap = new HashMap<>();
	Map<String, Integer> caterpillarSpeedMap = new HashMap<>();
}