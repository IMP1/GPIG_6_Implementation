package drones.sensors;

import network.ScanData;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import au.com.bytecode.opencsv.*;
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
	
	private static final double MAX_DIST = 50.0;

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
	
	private static double lastTime = System.nanoTime() / 1_000_000_000.0; 
	public static double getBatteryLevel() {
		final float battery_per_second = 0.01f;
		double currentTime = System.nanoTime() / 1_000_000_000.0;
		double dt = currentTime - lastTime;
		lastTime = currentTime;
		return 70 - dt * battery_per_second;
	}
	
	public static boolean isBatteryTooLow() {
		//XXX: some function of distance from the C2? 
		return SensorInterface.getBatteryLevel() < 0.4;
	}
	
	
	// TODO: Set GPS via 'Navigation'

	// TODO: Create and read in pre-defined sonar, depth and flow data
	public static ScanData getDataForPoint(double lat, double lon){
		CSVReader reader = null;
		double[] output = new double[360];
		ScanData outputs = null;
		Collection<MapObject> edgeList = null;
		

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(MapObject.class, new MapObjectDeserialiser());
		Gson gson = gsonBuilder.create();
		JsonParser parser = new JsonParser();
		JsonArray json = null;
		BufferedReader file = null;
		
		
		try{
			reader = new CSVReader(new FileReader("../sonar.csv"));
		}
		catch (Exception e){
			// ohnoes
			System.out.println(e.getMessage());
		}
				
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
		
		// For the demo, we will just go on the point we have which is the best approximation
		// to a 'scan'. So we look at all points, and select one based on 
		// a) How close it approximates the direction the sensor is *supposed* to be pointing at
		// b) How close it is to the drone.
		// For example, if we are meant to be at 5 degrees, and there are two points, one at 7 and 
		// one at 6, but 7 is closer (within the scan radius), pick 7. This should stop spurious
		// `long' positioning.
		for(int i = 0; i < 360; i++){
			double rad = Math.toRadians(i);
			double rad0 = mToD(10);
			double angmax = Double.MAX_VALUE;				
			double x1 = lat;
			double y1 = lon;
			double x2 = lat + 0;
			double y2 = lon + rad;
			//double angmax = 0;
			
			for (MapObject edge : edgeList) {
				System.out.println(edge.lat.toString());
				for(int j = 0; j < edge.lat.size(); j++){
					double x3 = edge.lat.get(j);
					double y3 = edge.lng.get(j);
					
					// We have two points, now to find the distance between them
					// ... and the angle
					
					double dx = x3 - x1;
					double dy = y3 - y1;
					
					double hypm = latLongDiffInMeters(dx, dy);
					
					double hyp = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
							
					double ang = Math.atan2(dx, dy);
					if (ang < 0.0){
						ang = (2 * Math.PI) + ang;
					}
					
					//System.out.println("Deg: " + Math.toDegrees(ang) + " Dist: " + hypm);
					
					double angdiff = rad - ang;
					
					if (Math.abs(angdiff) < angmax && hypm < MAX_DIST){
						System.out.println(angdiff + "Update closest at angle: " + i + " actual: " + Math.toDegrees(ang) + " Dist: " + hypm);
						angmax = angdiff;
						output[i] = hypm;
					}
					
					/*if (dx >= 0.0){
						if (dy >= 0.0){
							// Quadrant 1. Apply standard trig rules
							//     dx
							//   .---o
							// dy|  /
							//   |@/
							//   D/
							
							ang = Math.tan(dy / dx);
						}
						else if (dy < 0.0){
							// Quadrant 2.
							//
							// x2,y2
							//   o
							//   |\B
							// 10| \
							//   |  \
							//   D@  \
							//   |\_  \
							// dy|  h\_\
							//   |     \\
							//   '-------o
							//    dx
							
							// Calculate B
							double ndy = rad0 + Math.abs(dy);
							
							double b = Math.sqrt(Math.pow(ndy, 2) + Math.pow(dx, 2));
							
							// Using the cosine rule: Cos@ = (h^2 + rad0 ^ 2 - b  ^ 2 /) (2 * h * rad0)
							// 
							double cosang = (Math.pow(hyp, 2) + Math.pow(rad0, 2) - Math.pow(b, 2)) / (2 * hyp * rad0);
							
						}
					}
					else if (dx < 0.0){
						if (dy < 0.0){
							// Quadrant 3.
							//
							//       Bo
							//       /|
							//      / |10
							//     /  |
							//    /  _D@
							//   / _/ |
							//  /_/h  |dy
							// //     |
							//o-------'
							// A dx
							//
							
							// This time, we calculate the large RHS A and B. 
						}
					}*/
					
					
				}
			}
			
		}
		
		
		
		/*for (MapObject edge : edgeList) {
			System.out.println(edge.lat.toString());
			for(int i = 0; i < edge.lat.size(); i++){
				System.out.println(edge.lat.get(i).toString() + "," + edge.lng.get(i).toString());
				
			}
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
			double hyp = mToD(10);
			
		
			for(int i = 0; i < 360; i++){
				double rad = Math.toRadians(i);
				double dx = Math.cos(rad) * hyp;
				double dy = Math.sin(rad) * hyp;
				
				double x1 = lat;
				double y1 = lon;
				double x2 = lat + dx;
				double y2 = lon + dy;
				
				// We have our first line. We now need to calculate the second from a set of two points
				// x3,y3 shall be the points at the counter. 
				// x4,y4 shall be the points at the counter + 1. Remember this may not be a polygon, so 
				// 		 for this reason we can't wrap around!
				double maxdist = Double.MAX_VALUE;
				for(int j = 0; j < edge.lat.size() - 1; j++){
					double x3 = edge.lat.get(j);
					double y3 = edge.lng.get(j);
					double x4 = edge.lat.get(j+1);
					double y4 = edge.lng.get(j+1);
					
					// We have our two lines, now we can calculate the intersection between them (if any)
					// From Wikipedia...
					double px = ((((x1 * y2) - (y1 * x2)) * (x3 - x4)) - ((x1 - x2) * ((x3 * y4) - (y3 * x4)))) 
							  / ( ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4)) );
					double py = ((((x1 * y2) - (y1 * x2)) * (y3 - y4)) - ((y1 - y2) * ((x3 * y4) - (y3 * x4)))) 
							  / ( ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4)) );
					
					//System.out.println("Intersection :" + px + "," + py);
					
					// We have our intersection point, now to calculate the distance back to x1,x2
					dx = x1 - px;
					dy = y1 - py;
					double dist = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
					//System.out.println("Drone is at : "+ x1 + "," + y1 + "deg " + i);
					//System.out.println("Distance is : " + dx + "," + dy);
					//System.out.println("Intersection :" + px + "," + py + " Dist : " + dist);
					
					// Because this assumes that the lines may be infinite, we need to bound the result.
					// First, it may be 180 degrees behind the direction we are 'looking' in.
					// We'll bodge around this by saying the drone has sensors on the front and the back.
					// But we'll still return 360 points because 'redundancy'.
					// TODO: This currently means the returned sensor data is "mirrored". Needs fixing in 
					// 		 order to get more realistic scan data returns.
					
					// Convert back to metres
					double distm = latLongDiffInMeters(dx, dy);
					//System.out.println("Distance in metres is:" + distm);
					if( distm < maxdist){
						maxdist = distm;
					}

					
				}
				//System.out.println("At degrees " + i + " distance is " + maxdist);
				output[i] = maxdist;
			}
				
				
			
			

		}*/
		outputs = new ScanData(Drone.ID, java.time.LocalDateTime.now(), lat, lon, 1.0, 1.0, output);
		// So, we are point lat, lon. For 360 degrees from this point, find the nearest edge.
		
		
		
		
		String [] line;
		/*try {
			while ((line = reader.readNext()) != null){
				if(Double.parseDouble(line[0]) == lat && Double.parseDouble(line[1]) == lon){
					System.out.println("Hurray");
					System.out.println("Depth: " + line[2] + " Flow: " + line [3]);
					for (int i = 4; i < line.length; i++){
						
						System.out.print(line[i] + ",");
					}
					System.out.println();
					
					for (int i = 0 ; i < line.length - 4; i++){
						output[i] = Double.parseDouble(line[i + 4]);
					}
					outputs = new ScanData(Drone.ID, java.time.LocalDateTime.now(), lat, lon, Double.parseDouble(line[2]), Double.parseDouble(line[3]), output);
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (outputs == null){
			outputs = new ScanData(Drone.ID, java.time.LocalDateTime.now(), lat, lon, 5.0, 2.5, output);
		}*/


		
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
