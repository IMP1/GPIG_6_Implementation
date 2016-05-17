package drones.routing;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.graphhopper.PathWrapper;

import drones.Drone;
import drones.MapHelper;
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
	
	// Thread pool available
	private ExecutorService threadPool;

	/**
	 * Public constructor. Initialises thread pool.
	 */
	public RoutingHandler() {
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
		Callable<PathWrapper> task = new Router(lat, lng, radius);
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
		Executors.newSingleThreadExecutor().execute(new Runnable() {
		    @Override 
		    public void run() {
		    	Callable<PathWrapper> task = new Router(lat, lng, radius);
		    	Future<PathWrapper> route = threadPool.submit(task);
		    	try {
		    		// Block until complete
		    		Drone.nav().redirect(route.get(), lat, lng, radius);
		    	} catch (Exception e) {
		    		System.err.println(e.getMessage());
		    	}
		    }
		});
	}
	
	/**
	 * Routing delegate.
	 */
	@SuppressWarnings("unused")
	private class Router implements Callable<PathWrapper> {

		// Destination parameters
		double lat, lng, radius;
		
		/**
	 	 * Configures router with necessary parameters to perform the query
	 	 * @param lat Destination latitude in degrees
	 	 * @param lng Estimated carrying capacity of a west african swallow
	 	 * @param radius Radius size of area to scan
	 	 */
		public Router(double lat, double lng, double radius) {
			this.lat = lat;
			this.lng = lng;
			this.radius = radius;
		}
		
		/**
		 * Route using pre-defined parameters
		 */
		public PathWrapper call() {
			return MapHelper.route(SensorInterface.getGPSLatitude(),
					SensorInterface.getGPSLongitude(), lat, lng);
		}
	}
}
