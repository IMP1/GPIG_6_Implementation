package datastore;

import java.util.HashMap;
import com.google.gson.*;

public class Datastore {
	private HashMap<String, Drone> drones;
	private HashMap<String, Scan> scans;
	
	public Datastore(){
		drones = new HashMap<String,Drone>();
		scans = new HashMap<String, Scan>();
	}
	
	public boolean droneExists(String id){
		return drones.containsKey(id);
	}
	public Drone getDroneById(String id){
		return drones.get(id);
	}
	public void addDrone(String id, Drone drone){
		drones.put(id, drone);
	}
	
	public boolean scanExists(String id){
		return scans.containsKey(id);
	}
	public void addScan(String id, Scan scan) {
		scans.put(id,  scan);
		
	}
	public String getDronesAsJSON(){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(drones);
	}
}
