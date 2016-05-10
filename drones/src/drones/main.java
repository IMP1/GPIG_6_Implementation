package drones;

import java.io.File;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

/**
 * Main startup sequence.
 * Handles thread creation and resource initialisation.
 * Author: Martin Higgs
 */
public class main {
	
	private static GraphHopper hopper;

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
		
		// Test calculation of route
		
	}

}
