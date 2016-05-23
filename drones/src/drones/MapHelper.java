package drones;


import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;

import drones.navigation.NavigationThread;
import drones.util.*;
import network.ScanData;

/**
 * Abstract helper class containing static methods
 * for getting more complex information from the shared map.
 * @author Martin Higgs
 */
public abstract class MapHelper {
	
	private static class DronePosition {
		public final double latitude;
		public final double longitude;
		public final LocalDateTime time;
		private DronePosition(LocalDateTime time, double lat, double lng) {
			this.time = time;
			this.latitude = lat;
			this.longitude = lng;
		}
	}
	
	private static ArrayList<ScanData> scanDataList = new ArrayList<ScanData>();
	private static HashMap<String, DronePosition> dronePositions = new HashMap<String, DronePosition>();
	
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
		boolean internal = false;
		
		// Loop until an external point is found
		do {
			internal = false;
			for (MapObject b : buildingList) {
				// Break out of sorted list early
				if (b.minLat >= lat)
					break;
				// Otherwise run the check if inside the bounding box
				else if (b.minLng < lng && b.maxLat > lat && b.maxLng > lng) {
					// Draw line from outside bouding box to point
					double outLat = b.minLat - ((b.maxLat - b.minLat) / 100);
					double outLng = lng;
					
					// Count number of walls crossed to get to point
					int j = 1;
			    	for (int i = 0; i < b.lat.size(); i++) {
			    		j = (i + 1) % b.lat.size();
			    		if (Line2D.linesIntersect(outLat, outLng, lat, lng,
		   						b.lat.get(i), b.lng.get(i), b.lat.get(j), b.lng.get(j))) {
			    			internal = !internal;
			    		}
			    	}
			    	
			    	// If uneven number of walls crossed, the point is inside the building
			    	if (internal) {
			    		// Move to a building edge and check for overlap with other buildings
			    		Random rnd = new Random();

			    		// Pick a random edge
			    		int p = rnd.nextInt(b.lat.size());
			    		double latDiff = b.lat.get(p) - b.lat.get((p + 1) % b.lat.size());
			    		double lngDiff = b.lng.get(p) - b.lng.get((p + 1) % b.lng.size());

			    		// Pick a random amount along that edge
			    		float portion = rnd.nextFloat();
			    		lat = b.lat.get(p) + (portion * latDiff);
			    		lng = b.lng.get(p) + (portion * lngDiff);
			    		
			    		break;
			    	}
				}
			}
		} while (internal);

		double[] result = {lat, lng};
		return result;
	}

	public static void addScan(ScanData scanData) {
		synchronized (scanDataList) {
			scanDataList.add(scanData);
		}
	}
	
	public static void updateDronePosition(String id, LocalDateTime time, double lat, double lng) {
		synchronized (dronePositions) {
			if (!dronePositions.containsKey(id) || dronePositions.get(id).time.isBefore(time)) {
				DronePosition newPosition = new DronePosition(time, lat, lng);
				dronePositions.put(id, newPosition);
			}
		}
	}
	
	public static double[][] getDronePositions() {
		synchronized (dronePositions) {
			String[] droneIDs = dronePositions.keySet().toArray(new String[dronePositions.size()]);
			double[][] positions = new double[dronePositions.size()][2];
			for (int i = 0; i < droneIDs.length; i ++) {
				String id = droneIDs[i];
				positions[i][0] = dronePositions.get(id).latitude;
				positions[i][1] = dronePositions.get(id).longitude;
			}
			return positions;
		}
	}
	
	// Drone separation distance in meteres
	public static double DRONE_SEPARATION = 50.0;
	
	
	// Scan separation distance in meters
	public static double SCAN_SEPARATION = 5.0;
	
	/**
	 * Check if a scan has been performed near the requested location
	 * @param lat Latitude requested in degrees
	 * @param lng Longitude requested in degrees
	 * @return True if within SCAN_SEPARATION of a previous scan. False otherwise.
	 */
	public static boolean isScanned(double lat, double lng) {
		synchronized (scanDataList) {
			for (ScanData scan : scanDataList) {
				if (NavigationThread.latLongDiffInMeters
						(scan.latitude - lat, scan.longitude - lng) < SCAN_SEPARATION)
					return true;
			}
		}
		return false;
	}

	/**
	 * Check if another drone is near the requested scan location
	 * @param lat Latitude requested in degrees
	 * @param lng Longitude requested in degrees
	 * @return True if within DRONE_SEPARATION of another drone. False otherwise.
	 */
	public static boolean isNearDrone(double lat, double lng) {
		synchronized (dronePositions) {
			for (String droneId : dronePositions.keySet()) {
				DronePosition drone = dronePositions.get(droneId);
				if (NavigationThread.latLongDiffInMeters
						(drone.latitude - lat, drone.longitude - lng) < DRONE_SEPARATION)
					return true;
			}
		}
		return false;
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
		// Perform min / max calculations
		double minLat = srcLat < dstLat ? srcLat : dstLat;
		double minLng = srcLng < dstLng ? srcLng : dstLng;
		double maxLat = srcLat > dstLat ? srcLat : dstLat;
		double maxLng = srcLng > dstLng ? srcLng : dstLng;

		// Check for barrier intersection
		for (MapObject b : barrierList) {
			// Break out of sorted list early
			if (b.minLat >= maxLat)
				break;
			// Otherwise run the check if inside the bounding box
			else if (!(b.minLat >= maxLat || b.minLng >= maxLng
					|| b.maxLat <= minLat || b.maxLng <= minLng)) {
		   		for (int i = 0; i < (b.lat.size() - 1); i++) {
		   			if (Line2D.linesIntersect(minLat, minLng, maxLat, maxLng,
		   					b.lat.get(i), b.lng.get(i), b.lat.get(i+1), b.lng.get(i+1)))
		   				return true;
		   		}
			}
		}
		
		// Check for building intersection
		for (MapObject b : buildingList) {
			// Break out of sorted list early
			if (b.minLat >= maxLat)
				break;
			// Otherwise run the check if inside the bounding box
			else if (!(b.minLat >= maxLat || b.minLng >= maxLng
					|| b.maxLat <= minLat || b.maxLng <= minLng)) {
		   		for (int i = 0; i < b.lat.size(); i++) {
		   			int j = (i + 1) % b.lat.size();
		   			if (Line2D.linesIntersect(minLat, minLng, maxLat, maxLng,
		   					b.lat.get(i), b.lng.get(i), b.lat.get(j), b.lng.get(j)))
		   				return true;
		   		}
			}	
		}
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
