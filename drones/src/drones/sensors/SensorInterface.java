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
	private static final double DEFAULT_GPS_LAT = 53.957184;
	private static final double DEFAULT_GPS_LONG = -1.078302;
	
	// Static demo variables for modification
	private static double gpsLat = DEFAULT_GPS_LAT;
	private static double gpsLng = DEFAULT_GPS_LONG;

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
		
		try {
			file = new BufferedReader(new FileReader("../sensor_edge.geojson"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		json = parser.parse(file).getAsJsonObject().getAsJsonArray("features");
		
		try{
			reader = new CSVReader(new FileReader("../sonar.csv"));
		}
		catch (Exception e){
			// ohnoes
			System.out.println(e.getMessage());
		}
		
		for(int i = 0; i < json.size(); i++) {
			edgeList.add(gson.fromJson(json.get(i), MapObject.class));
		}
		Collections.sort((List<MapObject>) edgeList);
		
		
		String [] line;
		try {
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
		}


		
		return outputs;
	}
}
