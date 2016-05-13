package drones.scanner;

/**
 * Asynchronous scanning handler.
 * Placed in a separate thread as raw data from sensors will likely
 * need processing, though it is given as pre-processed information in
 * this prototype.
 * @author Anthony Williams
 */
public class ScannerHandler implements Runnable {
	
	public class Scan {
		public final double lat, lon, depth, flow;
		public final double[] distanceReadings;
		
		private Scan(double lat, double lon, double depth, double flow, double[] distanceReadings) {
			this.lat = lat;
			this.lon = lon;
			this.depth = depth;
			this.flow = flow;
			this.distanceReadings = distanceReadings;
		}
	}

	@Override
	public void run() {
		//TODO: Asynchronous scanner handling and giving data to transmit.
	}
}
