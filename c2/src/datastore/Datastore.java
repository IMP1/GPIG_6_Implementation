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
import gpig.all.schema.Point;
import gpig.all.schema.Polar;
import gpig.all.schema.datatypes.Blockage;
import gpig.all.schema.datatypes.Delivery;
import gpig.all.schema.datatypes.StrandedPerson;
import network.Message;
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
		Drone c2 = new Drone(100.0, 53.95457672001171, -1.0792994499206543, DroneState.IDLE, LocalDateTime.now());
		drones.put(Message.C2_ID, c2);
	}
	
	public synchronized HashMap<String, Scan> getScans(){
		return (HashMap<String, Scan>) scans.clone();
	}
	
	public synchronized void addExternalData(ArrayList<GISPosition> positions){
		externalData = positions;
	}
	public synchronized String getExternalDataAsJson(){
		ArrayList<GISPosition> dataclone = (ArrayList<GISPosition>) externalData.clone();
		ArrayList<ExternalData> returndata = new ArrayList<ExternalData>();
		for(GISPosition position : dataclone){
			double loclat = 0.0;
			double loclong = 0.0;
			String type = "";
			if (position.position instanceof Polar) {
				Polar point = (Polar) position.position;
				loclat = point.point.latitude;
				loclong = point.point.longitude;
				if (position.payload instanceof Delivery) {
					type = "Delivery";
				}
				if (position.payload instanceof StrandedPerson) {
					type = "StrandedPerson";
				}
				if (position.payload instanceof Blockage) {
					type = "Blockage";	
				}
			}
			if (position.position instanceof Point) {
				Point point = (Point) position.position;
				loclat = point.coord.latitude;
				loclong = point.coord.longitude;
				if (position.payload instanceof Delivery) {
					type = "Delivery";
				}
				if (position.payload instanceof StrandedPerson) {
					type = "StrandedPerson";
				}
				if (position.payload instanceof Blockage) {
					type = "Blockage";
				}
			}
			ExternalData data = new ExternalData(loclat, loclong, type);
			returndata.add(data);
		}
		return gson.toJson(returndata);
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
	
	public synchronized boolean removeDrone(String id){
		if(drones.containsKey(id)){
			drones.remove(id);
			return true;
		}
		return false;
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
		HashMap<String,Scan> scantemp = (HashMap<String, Scan>) scans.clone();
		for(final String id: scantemp.keySet()){
			ArrayList<Coord> edges = new ArrayList<Coord>();
			int i = 0;
			
			//Concurrency yo.
			for(final double reading: scantemp.get(id).rawDistanceReadings){
				System.out.println(reading);
				if(reading < SensorInterface.MAX_DIST){
					Coord coord = new Coord();
					coord.latitude = (float) scantemp.get(id).distanceReadings[i*2];
					coord.longitude = (float) scantemp.get(id).distanceReadings[i*2+1];
					edges.add(coord);
				}
				i++;
			}
			edgesList.add(edges);
		}
		return edgesList;
	}
}
