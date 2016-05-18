package datastore;

import java.time.LocalDateTime;
import java.util.HashMap;
import com.google.gson.*;

import frontendserver.SearchArea;
import network.StatusData.DroneState;

public class Datastore {
	private HashMap<String, Drone> drones;
	private HashMap<String, Scan> scans;
	private SearchArea currentSearchArea;
	
	Gson gson;
	
	public Datastore(){
		drones = new HashMap<String,Drone>();
		scans = new HashMap<String, Scan>();
		gson = new GsonBuilder().disableHtmlEscaping().create();
		Drone c2 = new Drone(1.0,53.9461765, -1.0306976, DroneState.IDLE, LocalDateTime.now());
		drones.put("c2", c2);
	}
	
	
	public synchronized HashMap<String, Drone> getDrones(){
		return drones;
	}
	public synchronized boolean droneExists(String id){
		return drones.containsKey(id);
	}
	public synchronized Drone getDroneById(String id){
		return drones.get(id);
	}
	public synchronized void addDrone(String id, Drone drone){
		drones.put(id, drone);
	}
	
	public synchronized boolean scanExists(String id){
		return scans.containsKey(id);
	}
	public void addScan(String id, Scan scan) {
		scans.put(id,  scan);
		
	}
	
	public synchronized void setSearchArea(SearchArea search){
		currentSearchArea = search;
	}
	
	public synchronized SearchArea getSearchArea(){
		return currentSearchArea;
	}
	
	public synchronized String getDronesAsJSON(){
		
		return gson.toJson(drones);
	}
	
	public synchronized Integer getNumberOfDrones(){
		return drones.size()-1; //bcos of C2
	}
	
	public synchronized String getScansAsJSON(String[] known_scans){
		HashMap<String, Scan> temp = (HashMap<String, Scan>) scans.clone();
		for (final String id : known_scans) {
			System.out.println(id);
			temp.remove(id);
		}
		return gson.toJson(temp);
		
	}
}
