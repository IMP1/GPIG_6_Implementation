package drones.navigation;

import java.util.Random;

import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.PointList;

import drones.Drone;
import drones.MapHelper;
import drones.sensors.SensorInterface;

/**
 * Persistent navigation thread.
 * Navigates to designated area then explores in a random fashion
 * whilst avoiding previously scanned areas and other drones.
 * @author Martin Higgs
 */
public class NavigationThread extends Thread {
	
	// Navigation types
	public static enum NavStatus {
		ROUTE_TO_TARGET_AREA,
		ROUTE_TO_CHECK_LOCATION,
		BUMBLING
	}
	
	// Number of short range checks to make before looking long distance
	private static final int LOCAL_CHECK_LIMIT = 50;
	// Seed for random number generator replicability
	private static final long SEED = 32143786234L;
	// Long and short range check distances (in meters)
	private static final int SHORT_DST_CHECK = 2;
	private static final int LONG_DST_CHECK = 10;
	
	// Current location
	private double currLat, currLng;
	// Location to check
	private double chkLat, chkLng;
	
	// Current target area
	private double tgtLat, tgtLng, tgtRadius;
	
	// Redirection information
	private boolean redirected = false;
	private PointList newRoute = null;
	private double newLat, newLng, newRadius;
	
	// Travel mode indicator
	private NavStatus routing = NavStatus.BUMBLING;
	private int routeStepIndex = 0;
	
	// Random number generator for exploration
	Random rnd = new Random(SEED);
	
	/**
	 * Thread initialisation.
	 * Starts at current location with no exploration to do.
	 */
	public NavigationThread() {
		tgtLat = SensorInterface.getGPSLatitude();
		tgtLng = SensorInterface.getGPSLongitude();
		tgtRadius = 0.0;
	}
	
	/**
	 * Main exploration loop.
	 * Continuously explores target area until there exist no more points to scan.
	 * When redirected to a new area, navigate along the defined path and then
	 * return to exploring.
	 */
	public void run() {
		while(true) {
			if(!checkForRedirect()) {
				// Get current location
				currLat = SensorInterface.getGPSLatitude();
				currLng = SensorInterface.getGPSLongitude();
				
				// Check if area already scanned and request scan if not
				try {
					// TODO: Check map and make request
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.println(e);
				}
				
				// Randomly select points until significantly far enough away from
				// other scans / drones or interrupted
				int chkCount = 0;
				chkLat = currLat;
				chkLng = currLng;
				boolean waypointFound = false;
				while(!waypointFound && !checkForRedirect()) {
					double angle = 2 * Math.PI * rnd.nextDouble();

					// After a certain number of short range hops are tried, start
					// looking at long range hops
					if (chkCount < LOCAL_CHECK_LIMIT) {
						chkLat += Math.sin(angle) * SHORT_DST_CHECK;
						chkLng += Math.cos(angle) * SHORT_DST_CHECK;
					} else {
						chkLat += Math.sin(angle) * LONG_DST_CHECK;
						chkLng += Math.cos(angle) * LONG_DST_CHECK;
					}

					// Move point to nearest outdoors location if indoors
					double[] point = MapHelper.getExternalPoint(chkLat, chkLng);
					chkLat = point[0];
					chkLng = point[1];

					// If location outside search area, reset search to drone location.
					if(Math.pow(tgtLat - chkLat, 2) + Math.pow(tgtLng - chkLng, 2) < tgtRadius) {
						chkLat = currLat;
						chkLng = currLng;
					}
					
					// TODO: Check if point is viable for scanning
				}
				// TODO: Check for routing necessity based on structure intersection
			}
				
			if(checkForRedirect()) {
				routing = NavStatus.ROUTE_TO_TARGET_AREA;
				routeStepIndex = 0;
				acknowledgeRedirect();
				
				// TODO: Accept routing for navigation (watch for race conditions!)
			}

			// Follow routing if required
			if(routing != NavStatus.BUMBLING) {
				// TODO: Follow set of waypoints specified by route and finally target
			}
			
			// TODO: Travelling abstraction. Assume constant movement speed.
		}
	}

	/**
	 * Set redirection notice for thread to pick up and set new parameters
	 * @param route Route to follow to new scan area
	 * @param lat Latitude of new target in degrees
	 * @param lng Longitude of new target in degrees
	 * @param radius Radius of new target area
	 */
	synchronized public void redirect(PathWrapper route, double lat, double lng, double radius) {
		redirected = true;
		newRoute = route.getPoints();
		newLat = lat;
		newLng = lng;
		newRadius = radius;
	}
	
	/**
	 * Check redirection notice
	 * @return Whether the drone should change target
	 */
	synchronized private boolean checkForRedirect() {
		return redirected;
	}
	
	/**
	 * Clear redirection notice
	 */
	synchronized private void acknowledgeRedirect() {
		redirected = false;
	}
}
