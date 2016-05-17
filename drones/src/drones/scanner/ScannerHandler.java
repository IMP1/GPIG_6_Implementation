package drones.scanner;

import drones.Drone;
import drones.sensors.SensorInterface;

/**
 * Asynchronous scanning handler.
 * Placed in a separate thread as raw data from sensors will likely
 * need processing, though it is given as pre-processed information in
 * this prototype.
 * @author Anthony Williams
 */
public class ScannerHandler implements Runnable {
	
	@Override
	public void run() {
		Drone.mesh().addScan(SensorInterface.getDataForPoint(SensorInterface.getGPSLatitude(), SensorInterface.getGPSLongitude()));
	}
}
