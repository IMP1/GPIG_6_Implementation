package drones;

import java.io.File;
import java.util.UUID;

import network.StatusData.DroneState;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

import drones.mesh.MeshInterfaceThread;
import drones.navigation.NavigationThread;
import drones.sensors.SensorInterface;

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
	private static DroneState state = DroneState.IDLE;
	
	// Singleton accessors
	// TODO: Handle synchronous read / write access to map (move to MapHelper and make protected?)
	public static GraphHopper map() {
		return map;
	}
	public static NavigationThread nav() {
		return navThread;
	}
	public static MeshInterfaceThread mesh() {
		return meshThread;
	}
	
	public static DroneState state() {
		synchronized (Drone.state) {
			return Drone.state;
		}
	}
	
	public static void setState(DroneState state) {
		//TODO: unlimit this?
		//      limit it further?
		synchronized (Drone.state) {
			if (!SensorInterface.isBatteryTooLow()) {
				Drone.state = state;
			}
		}
	}

	/**
	 * Entry point. Initialises singletons and control threads.
	 * ***WARNING***
	 * The threads are created in the following order 
	 * <em>for a reason</em>. The navigation thread refers to the
	 * mesh thread upon initialisation, so it needs to exist or a 
	 * null pointer exception is thrown.
	 * 
	 * @param args Relative path to OSM map. 
	 * 		Defaults to "../york.osm" for testing
	 */
	public static void main(String[] args) {
		// Configure shared routing
		map = new GraphHopper().forDesktop();
		// File locations relative to working dir
		if ((args.length < 1) || !(new File(args[0]).exists())) {
			map.setOSMFile("../york.osm");
			MapHelper.initialise("york");
		} else {
			map.setOSMFile(args[0]);
			MapHelper.initialise(args[0]);
		}
		map.setGraphHopperLocation("graph"); // Graph data storage
		
		// Enable unrestricted movement and load graph
		map.setEncodingManager(new EncodingManager("foot"));
		map.importOrLoad();
		System.out.println("Map loaded.");

		// Initialise mesh interface thread
		meshThread = new MeshInterfaceThread();
		
		// Initialise navigation thread
		navThread = new NavigationThread();
		
		// Start the threads
		meshThread.start();
		System.out.println("Mesh Interface started.");
		navThread.start();
		System.out.println("Navigation Thread started.");
		
		if (args.length >= 2) {
			//TODO: handle the arguments and induce some faults!
		}
	}
	
}
