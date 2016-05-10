package drones;

import java.io.File;
import java.util.concurrent.Future;

import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EncodingManager;

import drones.navigation.NavigationThread;
import drones.routing.RoutingHandler;
import drones.scanner.ScannerHandler;

/**
 * Main startup sequence and singleton handler.
 * Handles thread creation and resource initialisation.
 * Author: Martin Higgs
 */
public class Drone {
	
	// Singleton instances
	private static GraphHopper map = null;
	private static RoutingHandler router = null;
	private static NavigationThread navThread = null;
	private static ScannerHandler scanner = null;
	
	// Singleton accessors
	// TODO: Handle synchronous read / write access to map
	public static GraphHopper map() {
		return map;
	}
	public static RoutingHandler router() {
		return router;
	}
	public static NavigationThread nav() {
		return navThread;
	}
	public static ScannerHandler scanner() {
		return scanner;
	}

	/**
	 * Entry point. Initialises singletons and control threads.
	 * @param args Relative path to OSM map. 
	 * 		Defaults to "../york.osm" for testing
	 */
	public static void main(String[] args) {
		// Configure shared map
		map = new GraphHopper().forDesktop();
		// File locations relative to working dir
		if ((args.length < 1) || !(new File(args[0]).exists()))
			map.setOSMFile("../york.osm");
		else
			map.setOSMFile(args[0]);
		map.setGraphHopperLocation("graph"); // Graph data storage
		
		// Enable unrestricted movement and load graph
		map.setEncodingManager(new EncodingManager("foot"));
		map.importOrLoad();
		System.out.println("Graph loaded.");
		
		// Initialise routing handler
		router = new RoutingHandler();
		
		// Test calculation of route
		Future<PathWrapper> route = router.calculate(53.955391, -1.078967, 10.0);
		System.out.print("Calculating route");
		while(!route.isDone()) {
			System.out.print(".");
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.err.println("wtf?");
				System.err.println(e.getMessage());
			}
		}
		System.out.print("\n");
		try {
			PathWrapper path = route.get();
			System.out.println("Route calculated!");
			System.out.println("Distance: " + path.getDistance() + "m");
			System.out.println("Waypoints: " + path.getPoints().toString());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}
