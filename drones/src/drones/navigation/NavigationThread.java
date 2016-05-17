package drones.navigation;

import java.util.Random;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.PointList;

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
	// Constant travel speed
	private static final double MOVE_DISTANCE = 0.5;
	private static final int WAIT_TIME_MILLIS = 250;
	
	// Current location
	private double currLat, currLng;
	// Location to check
	private double chkLat, chkLng;
	// Next waypoint
	private double nxtLat, nxtLng;
	// Current route
	private PointList currRoute;
	
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
	@SuppressWarnings("deprecation")
	public void run() {
		while(true) {
			if(!checkForRedirect() && routing == NavStatus.BUMBLING) {
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
					chkCount += 1;

					// Move point to nearest outdoors location if indoors
					double[] point = MapHelper.getExternalPoint(chkLat, chkLng);
					chkLat = point[0];
					chkLng = point[1];

					// If location outside search area, reset search to drone location.
					if(latLongDiffInMeters(tgtLat - chkLat, tgtLng - chkLng) > tgtRadius) {
						chkLat = currLat;
						chkLng = currLng;
					}
					
					// TODO: Check if point is viable for scanning
				}

				// Check for routing necessity based on structure intersection
				if (!checkForRedirect()) {
					if (MapHelper.pathBlocked(currLat, currLng, chkLat, chkLng)) {
						routing = NavStatus.ROUTE_TO_CHECK_LOCATION;
						currRoute = MapHelper.route(currLat, currLng, chkLat, chkLng).getPoints();
					} else {
						routing = NavStatus.BUMBLING;
						nxtLat = chkLat;
						nxtLng = chkLng;
					}
				}
			}
				
			// Check for redirection before continuing too far
			if(checkForRedirect()) {
				// TODO: Replace synchronised with a lock. Currently can get fudged if redirected in this block!
				routing = NavStatus.ROUTE_TO_TARGET_AREA;
				routeStepIndex = 0;
				currRoute = newRoute;
				tgtLat = newLat;
				tgtLng = newLng;
				tgtRadius = newRadius;
				acknowledgeRedirect();
			}

			// Follow routing if required
			if(routing != NavStatus.BUMBLING) {
				if (routeStepIndex < currRoute.getSize()) {
					nxtLat = currRoute.getLatitude(routeStepIndex);
					nxtLng = currRoute.getLongitude(routeStepIndex);
					routeStepIndex += 1;
				} else if (routing == NavStatus.ROUTE_TO_TARGET_AREA) {
					nxtLat = tgtLat;
					nxtLng = tgtLng;
					routing = NavStatus.BUMBLING;
				} else {
					nxtLat = chkLat;
					nxtLng = chkLng;
					routing = NavStatus.BUMBLING;
				}
			}
			
			// Travelling abstraction. Assume constant movement speed.
			while (latLongDiffInMeters(nxtLat - currLat, nxtLng - currLng) > 0.01) {
				// Move
				if (latLongDiffInMeters(nxtLat - currLat, nxtLng - currLng) < MOVE_DISTANCE) {
					SensorInterface.setGPS(nxtLat, nxtLng);
				} else {
					double angle = Math.tanh((nxtLat - currLat) / (nxtLng - currLng));
					SensorInterface.setGPS(currLat + (Math.sin(angle) * mToD(MOVE_DISTANCE)), 
							currLng + (Math.cos(angle) * mToD(MOVE_DISTANCE)));
				}
				System.err.println("LAT: " + SensorInterface.getGPSLatitude() 
					+ ", LONG: " + SensorInterface.getGPSLongitude());

				// Wait
				try {
					Thread.sleep(WAIT_TIME_MILLIS);
				} catch (InterruptedException e) {
					System.err.println(e);
				}

				// Update position
				currLat = SensorInterface.getGPSLatitude();
				currLng = SensorInterface.getGPSLongitude();
			}
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
		System.err.println("REDIRECTED");
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
	
	/**
	 * Get the current route that the drone is following.
	 * @return Null if the drone is not currently following a route.
	 * 		Otherwise return the set of points it is following to the target.
	 */
	synchronized public PointList getCurrentPath() {
		if (routing == NavStatus.BUMBLING)
			return null;
		return currRoute;
	}
	
	// Earth's radius in meters for distance calculation
	private static final int EARTH_RADIUS = 6731000;
	
	/**
	 * Private helper for naively calculating distance in metres, ignores curvature of earth.
	 * @param latDiff Latitude difference in degrees
	 * @param longDiff Longitude difference in degrees
	 * @return Absolute difference in metres
	 */
	private static double latLongDiffInMeters(double latDiff, double longDiff) {
		double latDiffM = Math.tan(Math.toRadians(latDiff)) * EARTH_RADIUS;
		double longDiffM = Math.tan(Math.toRadians(longDiff)) * EARTH_RADIUS;
		double dist = Math.sqrt(Math.pow(latDiffM, 2) + Math.pow(longDiffM, 2));
		return dist;
	}
	
	/**
	 * Convert meters to degrees
	 * @param m Meters distance
	 * @return Degrees (ignoring curvature of earth
	 */
	private static double mToD(double m) {
		return m / EARTH_RADIUS;
	}
}
