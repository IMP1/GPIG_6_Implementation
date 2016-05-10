package drones.navigation;

import com.graphhopper.PathWrapper;

import drones.sensors.SensorInterface;

/**
 * Persistent navigation thread.
 * Navigates to designated area then explores in a random fashion
 * whilst avoiding previously scanned areas and other drones.
 * @author Martin Higgs
 */
public class NavigationThread extends Thread {
	
	// Current target area
	private double lat, lng, radius;
	
	// Redirection information
	private boolean redirected = false;
	private PathWrapper routeToTarget = null;
	private double newLat, newLng, newRadius;

	
	/**
	 * Thread initialisation.
	 * Starts at current location with no exploration to do.
	 */
	public NavigationThread() {
		lat = SensorInterface.getGPSLatitude();
		lng = SensorInterface.getGPSLongitude();
		radius = 0.0;
	}
	
	public void run() {
		// TODO: Navigation thread.
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
		routeToTarget = route;
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
