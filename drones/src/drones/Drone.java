package drones;

import java.io.File;
import java.util.UUID;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

import drones.mesh.MeshInterfaceThread;
import drones.navigation.NavigationThread;

/**
 * Main startup sequence and singleton handler.
 * Handles thread creation and resource initialisation.
 * Author: Martin Higgs
 */
public class Drone {
	
	public final static String ID = UUID.randomUUID().toString();
	
	// Singleton instances
	private static GraphHopper map = null;
	private static NavigationThread navThread = null;
	private static MeshInterfaceThread meshThread = null;
	
	// Singleton accessors
	// TODO: Handle synchronous read / write access to map
	public static GraphHopper map() {
		return map;
	}
	public static NavigationThread nav() {
		return navThread;
	}
	public static MeshInterfaceThread mesh() {
		return meshThread;
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
		
		// Initialise and release navigation thread
		navThread = new NavigationThread();
		navThread.start();
		System.out.println("Navigation Thread started.");

		// Initialise and begin mesh interface thread
		meshThread = new MeshInterfaceThread();
		meshThread.start();
		System.out.println("Mesh Interface created.");
	}

}
