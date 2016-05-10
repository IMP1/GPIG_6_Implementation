package drones.routing;

import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;

import drones.sensors.SensorInterface;

/**
 * Routing Handler object.
 * Creates threads for routing over the shared map
 * and asynchronously produces waypoints / distance
 * calculations.
 * 
 * @author Martin Higgs
 */
public class RoutingHandler {
	
	// Private reference to the shared map
	private GraphHopper map;
	// Thread pool available
	private ExecutorService threadPool;
	// Navigation component to direct
	// TODO: Implement and reference nav module

	/**
	 * Public constructor
	 * @param hopper Shared map to route over
	 */
	public RoutingHandler(GraphHopper hopper) {
		map = hopper;
		threadPool = Executors.newFixedThreadPool(2);
	}
	
	/**
	 * Asynchronously calculate the fastest route to the requested area
	 * @param lat Destination latitude in degrees
	 * @param lng Destination longitude in degrees
	 * @param radius Radius size of area to scan
	 * @return A future that can be inspected for the calculated
	 * 		route upon completion
	 */
	public Future<PathWrapper> calculate(double lat, double lng, double radius) {
		Callable<PathWrapper> task = new Router(lat, lng, radius, map);
		return threadPool.submit(task);
	}

	/**
	 * Asynchronously calculate the fastest route to the requested area
	 * 		and pass the details on to the navigation component
	 * @param lat Destination latitude in degrees
	 * @param lng Destination longitude in degrees
	 * @param radius Radius size of area to scan
	 */
	public void go(double lat, double lng, double radius) {
		// Start a new thread to automatically handle routing completion
		
	}
	
	/**
	 * Routing delegate.
	 */
	private class Router implements Callable<PathWrapper> {

		// Map to route over
		private GraphHopper map;
		// Destination parameters
		double lat, lng, radius;
		
		/**
	 	 * Configures router with necessary parameters to perform the query
	 	 * @param lat Destination latitude in degrees
	 	 * @param lng Estimated carrying capacity of a west african swallow
	 	 * @param radius Radius size of area to scan
	 	 * @param map Map over which to route
	 	 */
		public Router(double lat, double lng, double radius, GraphHopper map) {
			this.lat = lat;
			this.lng = lng;
			this.radius = radius;
			this.map = map;
		}
		
		/**
		 * Route using pre-defined parameters
		 */
		public PathWrapper call() {
			// Construct and make routing request
			GHRequest req = new GHRequest(SensorInterface.getGPSLatitude(),
					SensorInterface.getGPSLongitude(), lat, lng).
			    setWeighting("fastest").
			    setVehicle("foot").
			    setLocale(Locale.UK);
			GHResponse rsp = map.route(req);

			// Log errors and return null result if so
			if(rsp.hasErrors()) {
				for(Throwable e : rsp.getErrors())
					System.err.println(e.getMessage());
				return null;
			}

			// Return best route
			return rsp.getBest();
		}
	}
}
