package drones;

import java.io.File;
import java.util.concurrent.Future;

import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EncodingManager;

import drones.routing.RoutingHandler;

/**
 * Main startup sequence.
 * Handles thread creation and resource initialisation.
 * Author: Martin Higgs
 */
public class main {
	
	private static GraphHopper hopper;
	private static RoutingHandler router;

	/**
	 * Entry point.
	 * @param args Relative path to OSM map. 
	 * 		Defaults to "../york.osm" for testing
	 */
	public static void main(String[] args) {
		// Configure shared GraphHopper
		hopper = new GraphHopper().forDesktop();
		// File locations relative to working dir
		if ((args.length < 1) || !(new File(args[0]).exists()))
			hopper.setOSMFile("../york.osm");
		else
			hopper.setOSMFile(args[0]);
		hopper.setGraphHopperLocation("graph"); // Graph data storage
		
		// Enable unrestricted movement and load graph
		hopper.setEncodingManager(new EncodingManager("foot"));
		hopper.importOrLoad();
		System.out.println("Graph loaded.");
		
		// Initialise routing handler
		router = new RoutingHandler(hopper);
		
		// Test calculation of route
		Future<PathWrapper> route = router.calculate(53.955391, -1.078967, 10.0);
		System.out.print("Calculating route");
		while(!route.isDone()) {
			System.out.print(".");
			try {
				Thread.sleep(100);
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
