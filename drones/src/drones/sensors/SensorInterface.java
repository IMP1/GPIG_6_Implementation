package drones.sensors;

import network.ScanData;

import java.awt.geom.Line2D;
import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import drones.Drone;
import drones.util.MapObjectDeserialiser;
import drones.util.MapObject;


/**
 * Sensor interface.
 * All static methods as these will eventually be piped out to hardware.
 * For now, GPS is updated by the Navigation thread and sonar/depth/flow data 
 * are returned as absolute values from a pre-defined data set based on location.
 * @author Anthony Williams and Martin Higgs
 */
public abstract class SensorInterface {
	
	// Default location on startup
	private static final double DEFAULT_GPS_LAT = 53.955849;
	private static final double DEFAULT_GPS_LONG = -1.0800562;
	
	// Static demo variables for modification
	private static double gpsLat = DEFAULT_GPS_LAT;
	private static double gpsLng = DEFAULT_GPS_LONG;
	
	private static final double MAX_DIST = 20.0;

	/**
	 * Get the current latitude (via GPS)
	 * @return Current latitude in degrees
	 */
	public static double getGPSLatitude() {
		return gpsLat;
	}
	
	/**
	 * Get the current longitude (via GPS)
	 * @return Current longitude in degrees
	 */
	public static double getGPSLongitude() {
		return gpsLng;
	}
	
	/**
	 * DEMO FUNCTION FOR USE BY NAVIGATION THREAD ONLY.
	 * Set current location to return from GPS
	 * @param lat Latitude in degrees
	 * @param lng Longitude in degrees
	 */
	@Deprecated
	public static void setGPS(double lat, double lng) {
		gpsLat = lat;
		gpsLng = lng;
	}
	

	public static double getBatteryLevel() {
		final double max_battery = 0.7;
		final double min_battery = 0.6;
		return Math.random() * (max_battery - min_battery) + min_battery;
	}
	
	public static boolean isBatteryTooLow() {
		//XXX: some function of distance from the C2? 
		return SensorInterface.getBatteryLevel() < 0.4;
	}
	
	
	// TODO: Set GPS via 'Navigation'

	// TODO: Create and read in pre-defined sonar, depth and flow data
	public static ScanData getDataForPoint(double lat, double lon){

		double[] output = new double[360];
		ScanData outputs = null;
		Collection<MapObject> edgeList = null;
		

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(MapObject.class, new MapObjectDeserialiser());
		Gson gson = gsonBuilder.create();
		JsonParser parser = new JsonParser();
		JsonArray json = null;
		BufferedReader file = null;
		
		// Read in list of water edges 
		if (edgeList == null) {
			try {
				edgeList = new ArrayList<MapObject>();
				file = new BufferedReader(new FileReader("../sensor_edge.geojson"));
				json = parser.parse(file).getAsJsonObject().getAsJsonArray("features");
				for(int i = 0; i < json.size(); i++) {
					edgeList.add(gson.fromJson(json.get(i), MapObject.class));
				}
				Collections.sort((List<MapObject>) edgeList);
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
				
		for(int i = 0; i < 360; i++){

			// We want to check for the intersection of two lines...
			// So, we take our drone location to be x1,y1, and a position 10 metres away to be x2,y2
			// Then, for each vertex in our flood shape, take this to be x3,y3 and x4,y4
			//
			//                x2,y2
			//                 o
			// x3,y3          /            x4,y4
			//   o-----------X---------------o
			//              /
			//             /
			//            D
			//         x1,y1
			//
			// First, we need to calculate x2,y2 , which means adding 10m in the relevant direction
			// So, we need dx,dy. Basic trig time! (But remember to do it in Radians)
			// We know the hyp. and angle. So, dx = cos(theta) * hyp and dy = sin(theta) * hyp
			// Then, calculate x2,y2 by adding dx and dy to lat and lon respectively.
			
			double hyp = mToD(MAX_DIST);

			double rad = Math.toRadians(i);
			double dx = Math.cos(rad) * hyp;
			double dy = Math.sin(rad) * hyp;
			
			// x1,y1 -- the drone
			double x1 = lat;
			double y1 = lon;
			
			// x2,y2 -- the distance the drone can 'scan' to
			double x2 = lat + dx;
			double y2 = lon + dy;
						
			for (MapObject edge : edgeList) {
				
				// This is a bit of a hack, please don't look too hard.
				// We need to convert to a Polygon as it has a convenient 'contains' function
				// However, Polygon takes an array of ints, and we are dealing with differences in
				// the order of several decimal places. So, we scale it all up to integer size...
				// Surprisingly, this seems to work.
				
				int[] alat = new int[edge.lat.size()];
				int[] alng = new int[edge.lng.size()];
				for(int c = 0; c < edge.lat.size(); c++){
					alat[c] = dtoM(edge.lat.get(c));
					alng[c] = dtoM(edge.lng.get(c));
				}
				Polygon p = new Polygon(alat, alng, edge.lat.size());
				
				if(p.contains(dtoM(x1), dtoM(y1)) && p.contains(dtoM(x2), dtoM(y2))){
					// The max scan range of the drone is within the flood polygon, so we 
					// can't find the edge of it.
					output[i] = MAX_DIST;
				}
				if(p.contains(dtoM(x1), dtoM(y1)) && !p.contains(dtoM(x2), dtoM(y2))){
									
					// We know that the sonar can find the edge of the water polygon.
					// We now need to calculate the second from a set of two points
					// x3,y3 shall be the points at the counter. 
					// x4,y4 shall be the points at the counter + 1. Since this is a polygon, we 
					// wrap around at the end.
					
					for(int j = 0; j < edge.lat.size(); j++){
						
						// x3,y3 -- the first polygon point to make the line
						double x3 = edge.lat.get(j);
						double y3 = edge.lng.get(j);
						
						// x4,y4 -- the second set of points.
						double x4, y4;
						if (j < edge.lat.size() - 1){
							x4 = edge.lat.get(j+1);
							y4 = edge.lng.get(j+1);
						}
						else { // Wrap around to the first point
							x4 = edge.lat.get(0);
							y4 = edge.lng.get(0);
						}
						
						// Construct Line2D types and check for intersection.
						Line2D.Double polyline  = new Line2D.Double(x3, y3, x4, y4);
						Line2D.Double droneline = new Line2D.Double(x1, y1, x2, y2);
						if(polyline.intersectsLine(droneline)){
							
							// We have an intersection. Helpfully, Line2D will tell you `yes, there is one',
							// but neglects to tell you *WHERE*. Fortunately, Wikipedia does.
							// https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection#Given_two_points_on_each_line
							
							double px = ((((x1 * y2) - (y1 * x2)) * (x3 - x4)) - ((x1 - x2) * ((x3 * y4) - (y3 * x4)))) 
									  / ( ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4)) );
							double py = ((((x1 * y2) - (y1 * x2)) * (y3 - y4)) - ((y1 - y2) * ((x3 * y4) - (y3 * x4)))) 
									  / ( ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4)) );
														
							// We have our intersection point, now to calculate the distance back to x1,x2
							dx = x1 - px;
							dy = y1 - py;
							
							// Convert back to metres
							double distm = latLongDiffInMeters(dx, dy);
							output[i] = distm;

							}
					}		
				}
		
			}
				
				
			
			

		}
		outputs = new ScanData(Drone.ID, java.time.LocalDateTime.now(), lat, lon, 1.0, 1.0, output);
				
		return outputs;
	}
	
	
	// Earth's radius in meters for distance calculation
	// For reference, 1m is roughly 0.0000085 (8.5e^-6) degrees
	private static final int EARTH_RADIUS = 6731000;
	/**
	 * Convert meters to degrees
	 * @param m Meters distance
	 * @return Degrees (ignoring curvature of earth
	 */
	public static double mToD(double m) {
		double deg = Math.toDegrees(m / EARTH_RADIUS);
		return deg;
	}
	
	/**
	 * Convert degrees to metres
	 * @param d Degrees distance
	 * @return Metres (ignoring curvature of earth
	 */
	public static int dtoM(double d) {
		int met = (int) (Math.toRadians(d) * EARTH_RADIUS);
		return met;
	}
	
	/**
	 * Private helper for naively calculating distance in metres, ignores curvature of earth.
	 * @param latDiff Latitude difference in degrees
	 * @param longDiff Longitude difference in degrees
	 * @return Absolute difference in metres
	 */
	public static double latLongDiffInMeters(double latDiff, double longDiff) {
		double latDiffM = Math.toRadians(latDiff) * EARTH_RADIUS;
		double longDiffM = Math.toRadians(longDiff) * EARTH_RADIUS;
		double dist = Math.sqrt(Math.pow(latDiffM, 2) + Math.pow(longDiffM, 2));
		return dist;
	}
	
}
