package drones.sensors;

/**
 * Sensor interface.
 * All static methods as these will eventually be piped out to hardware.
 * For now, GPS is updated by the Navigation thread and sonar/depth/flow data 
 * are returned as absolute values from a pre-defined data set based on location.
 * @author Martin Higgs
 */
public class SensorInterface {
	public static double getGPSLatitude() {
		return 53.957184;
	}
	
	public static double getGPSLongitude() {
		return -1.078302;
	}

}
