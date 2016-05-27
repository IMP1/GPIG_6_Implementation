package datastore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.*;

import drones.sensors.SensorInterface;
import frontendserver.SearchArea;
import gpig.all.schema.Coord;
import gpig.all.schema.GISPosition;
import network.StatusData.DroneState;
import sun.management.Sensor;

public class Datastore {
	private HashMap<String, Drone> drones;
	private HashMap<String, Scan> scans;
	private SearchArea currentSearchArea;
	private ArrayList<GISPosition> externalData;
	
	public Gson gson;
	
	public Datastore(){
		drones = new HashMap<String,Drone>();
		scans = new HashMap<String, Scan>();
		externalData = new ArrayList<GISPosition>();
		gson = new GsonBuilder().disableHtmlEscaping().create();
		Drone c2 = new Drone(100.0,53.9461765, -1.0306976, DroneState.IDLE, LocalDateTime.now());
		drones.put("c2", c2);
	}
	
	public synchronized void addExternalData(ArrayList<GISPosition> positions){
		externalData = positions;
	}
	public synchronized String getExternalDataAsJson(){
		return gson.toJson(externalData);
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
		if(drones.containsKey(id)){
			return;
		}
		System.out.println("Creating new drone "+id);
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
	
	public synchronized String getScansAsJSON(LocalDateTime known_scans){
		HashMap<String, Scan> temp = (HashMap<String, Scan>) scans.clone();
		ArrayList<String> toPop = new ArrayList<String>();
		//Come up with a list of items not to bother returning.
		for (final String id : temp.keySet()) {
			if(temp.get(id).received.isBefore(known_scans) || temp.get(id).received.equals(known_scans)){
				toPop.add(id);
			}
		}
		for(final String id: toPop){
			temp.remove(id);
		}
		return gson.toJson(temp);	
	}


	public ArrayList<ArrayList<Coord>> getEdges() {
		ArrayList<ArrayList<Coord>> edgesList = new ArrayList<ArrayList<Coord>>();
		for(final String id: scans.keySet()){
			ArrayList<Coord> edges = new ArrayList<Coord>();
			int i = 0;
			for(final double reading: scans.get(id).rawDistanceReadings){
				System.out.println(reading);
				if(reading < SensorInterface.MAX_DIST){
					Coord coord = new Coord();
					coord.latitude = (float) scans.get(id).distanceReadings[i*2];
					coord.longitude = (float) scans.get(id).distanceReadings[i*2+1];
					System.out.println(coord.latitude +" "+coord.longitude);
					edges.add(coord);
				}
				i++;
			}
			edgesList.add(edges);
		}
		return edgesList;
	}
}
