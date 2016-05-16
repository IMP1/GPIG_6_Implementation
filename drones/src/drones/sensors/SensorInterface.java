package drones.sensors;

/**
 * Sensor interface.
 * All static methods as these will eventually be piped out to hardware.
 * For now, GPS is updated by the Navigation thread and sonar/depth/flow data 
 * are returned as absolute values from a pre-defined data set based on location.
 * @author Anthony Williams and Martin Higgs
 */
public abstract class SensorInterface {
	public static double getGPSLatitude() {
		return 53.957184;
	}
	
	public static double getGPSLongitude() {
		return -1.078302;
	}
	
	public static double getBatteryLevel() {
		final double max_battery = 0.7;
		final double min_battery = 0.6;
		return Math.random() * (max_battery - min_battery) + min_battery;
	}
	
	// TODO: Set GPS via 'Navigation'

	// TODO: Create and read in pre-defined sonar, depth and flow data
}
