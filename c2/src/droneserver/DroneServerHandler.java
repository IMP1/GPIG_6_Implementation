package droneserver;

import java.io.IOException;
import java.io.Reader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Date;

import broadcast.Broadcast;
import datastore.Datastore;
import datastore.Drone;
import datastore.Scan;
import network.Acknowledgement;
import network.Command;
import network.Message;
import network.ScanData;
import network.StatusData;

public class DroneServerHandler implements Runnable {
	
	protected String data;
	protected Datastore datastore;
	
	public DroneServerHandler(String data, Datastore datastore) throws IOException {
		this.data = data;
		this.datastore = datastore;
	}
	
	@Override
	public void run() {
		System.out.println("Drone Request Received, handler started");
		String id = null;
		LocalDateTime dt = null;
		//if type info
		System.out.println(data);
		if(Message.getType(data)==StatusData.class){
			StatusData statusdata = new StatusData(data);
			id = statusdata.id;
			dt = statusdata.timestamp;
			//if we've seen this drone before, update it
			if(datastore.droneExists(statusdata.id)){
				Drone drone = datastore.getDroneById(statusdata.id);
				LocalDateTime timestamp = statusdata.timestamp;
				LocalDateTime storeddate= drone.getTimestamp();
				if(timestamp.isAfter(storeddate)){
					System.out.println("Updating known drone "+statusdata.id);
					drone.setTimestamp(statusdata.timestamp);
					drone.setBatteryLevel(statusdata.batteryStatus);
					drone.setLocLat(statusdata.latitude);
					drone.setLocLong(statusdata.longitude);
					drone.setStatus(statusdata.status);
				}
				
			}else{
				//otherwise create it and chuck it in the datastore.
				System.out.println("Creating new drone "+statusdata.id);
				Drone drone = new Drone(statusdata.batteryStatus, statusdata.latitude, statusdata.longitude, statusdata.status, statusdata.timestamp);
				datastore.addDrone(statusdata.id, drone);
			}
		} else if (Message.getType(data)== ScanData.class){
			ScanData scandata = new ScanData(data);
			id = scandata.id;
			dt = scandata.timestamp;
			String ident = scandata.id+scandata.timestamp;
			if(!datastore.scanExists(ident)){
				System.out.println("Adding new scan data"+scandata.id+scandata.timestamp);
				Scan scan = new Scan(scandata.latitude, scandata.longitude, scandata.depth, scandata.flowRate, scandata.distanceReadings);
				datastore.addScan(ident, scan);
			}
		}
		//TODO - should this be moved?
		if(Message.getType(data) != Acknowledgement.class){
			Acknowledgement ack = new Acknowledgement(id, dt);
			Broadcast.broadcast(ack.toString());
		}
	}

}
