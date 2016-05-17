package drones;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;

import drones.util.*;

import network.ScanData;

/**
 * Abstract helper class containing static methods
 * for getting more complex information from the shared map.
 * @author Martin Higgs
 */
public abstract class MapHelper {
	
	private static ArrayList<ScanData> scanDataList = new ArrayList<ScanData>();
	
	// Impassable object lists
	private static Collection<MapObject> barrierList = null;
	private static Collection<MapObject> buildingList = null;
	
	/**
	 * Initialise the map helper lists if necessary
	 * @param location String title of files to load.
	 * 		(e.g. "york" will load "york_barriers.geojson" etc.)
	 */
	public static void initialise(String location) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(MapObject.class, new MapObjectDeserialiser());
		Gson gson = gsonBuilder.create();
		JsonParser parser = new JsonParser();
		JsonArray json = null;
		BufferedReader file = null;

		// Read in list of impassable barriers
		if (barrierList == null) {
			try {
				barrierList = new ArrayList<MapObject>();
				file = new BufferedReader(new FileReader("../" + location + "_barriers.geojson"));
				json = parser.parse(file).getAsJsonObject().getAsJsonArray("features");
				for(int i = 0; i < json.size(); i++) {
					barrierList.add(gson.fromJson(json.get(i), MapObject.class));
				}
				Collections.sort((List<MapObject>) barrierList);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (file != null)
					try {
						file.close();
					} catch (Exception e) {
						System.err.print(e);
					}
			}
		}
		
		// Read in list of buildings
		if (buildingList == null) {
			try {
				buildingList = new ArrayList<MapObject>();
				file = new BufferedReader(new FileReader("../" + location + "_buildings.geojson"));
				json = parser.parse(file).getAsJsonObject().getAsJsonArray("features");
				for(int i = 0; i < json.size(); i++) {
					buildingList.add(gson.fromJson(json.get(i), MapObject.class));
				}
				Collections.sort((List<MapObject>) buildingList);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (file != null)
					try {
						file.close();
					} catch (Exception e) {
						System.err.print(e);
					}
			}
		}
		
		// TODO: Add graph hopper to this bit.
	}
	
	/**
	 * Get the closest outdoors point to the specified location.
	 * If the location is outdoors already, then it is returned as is.
	 * @param lat Requested latitude in degrees
	 * @param lng Requested longitude in degrees
	 * @return Double array: [latitude, longitude]
	 */
	public static double[] getExternalPoint(double lat, double lng) {
		double[] result = {lat, lng};
		
		// TODO: Check if point is inside the building nodes
		// http://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon

		return result;
	}

	public static void addScan(ScanData scanData) {
		synchronized (scanDataList) {
			scanDataList.add(scanData);
		}
	}
	
	//TODO: functions used by navigation and routing regarding querying the list of scan data.

	/**
	 * Check if the direct path between 2 points is blocked.
	 * @param srcLat Starting latitude in degrees
	 * @param srcLng Starting longitude in degrees
	 * @param dstLat Destination latitude in degrees
	 * @param dstLng Destination longitude in degrees
	 * @return True if the path between the two points is blocked and
	 * 		requires routing. False otherwise.
	 */
	public static boolean pathBlocked(double srcLat, double srcLng, double dstLat, double dstLng) {
		// TODO: Check vector intersection
		return false;
	}
	
	/**
	 * Calculate a route from the start location to the destination.
	 * @param srcLat Starting latitude in degrees
	 * @param srcLng Starting longitude in degrees
	 * @param dstLat Destination latitude in degrees
	 * @param dstLng Destination longitude in degrees
	 * @return A pathwrapper including the route. Be warned, this does not include
	 * 		the start or destination locations however!
	 */
	public static PathWrapper route(double srcLat, double srcLng, double dstLat, double dstLng) {
		// Construct and make routing request
		GHRequest req = new GHRequest(srcLat, srcLng, dstLat, dstLng).
		    setWeighting("fastest").
		    setVehicle("foot").
		    setLocale(Locale.UK);
		GHResponse rsp = Drone.map().route(req);

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
