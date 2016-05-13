package drones;

import java.io.File;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.Future;

import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;

import drones.mesh.MeshInterfaceThread;
import drones.navigation.NavigationThread;
import drones.routing.RoutingHandler;
import drones.scanner.ScannerHandler;
import drones.sensors.SensorInterface;

/**
 * Main startup sequence and singleton handler.
 * Handles thread creation and resource initialisation.
 * Author: Martin Higgs
 */
public class Drone {
	
	// Singleton instances
	private static GraphHopper map = null;
	private static NavigationThread navThread = null;
	
	// Singleton accessors
	// TODO: Handle synchronous read / write access to map
	public static GraphHopper map() {
		return map;
	}
	public static NavigationThread nav() {
		return navThread;
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
			map.setOSMFile("../gpig_6_implementation/york.osm");
		else
			map.setOSMFile(args[0]);
		map.setGraphHopperLocation("graph"); // Graph data storage
		
		// Enable unrestricted movement and load graph
		map.setEncodingManager(new EncodingManager("foot"));
		map.importOrLoad();
		System.out.println("Graph loaded.");
		
		// Initialise routing handler
		RoutingHandler router = new RoutingHandler(); // TODO: Initialise in Mesh Interface
		
		// Initialise and release navigation thread
		navThread = new NavigationThread();
		navThread.start();
		
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
			PointList points = path.getPoints();
			for (int i = 0; i < points.size(); i++){
				System.out.println(points.getLat(i));
				System.out.println(points.getLon(i));
				SensorInterface.getDataForPoint(points.getLat(i), points.getLon(i));

			}
			//SensorInterface.getDataForPoint(53.95717145394973,-1.0783239204362758);
		} catch (Exception e) {
			System.out.println("whelp");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
