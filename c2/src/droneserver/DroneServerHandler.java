package droneserver;

import java.io.IOException;
import java.time.LocalDateTime;

import broadcast.Broadcast;
import datastore.Datastore;
import datastore.Drone;
import datastore.Scan;
import network.Acknowledgement;
import network.Message;
import network.PathData;
import network.ScanAcknowledgement;
import network.ScanData;
import network.StatusData;

public class DroneServerHandler implements Runnable {
	private static final int EARTH_RADIUS = 6731000;
	protected String data;
	protected Datastore datastore;
	
	public DroneServerHandler(String data, Datastore datastore) throws IOException {
		this.data = data;
		this.datastore = datastore;
	}
	
	@Override
	public void run() {
//		System.out.println("Drone Request Received, handler started");
		String id = null;
		LocalDateTime dt = null;
		//if type info
//		System.out.println(data);
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
//					System.out.println("Updating known drone "+statusdata.id);
					drone.setTimestamp(statusdata.timestamp);
					drone.setBatteryLevel(statusdata.batteryStatus);
					drone.setLocLat(statusdata.latitude);
					drone.setLocLong(statusdata.longitude);
					drone.setStatus(statusdata.status);
					drone.setPath(statusdata.currentPath);
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
//			System.out.println("Receiving scandata"+id);
			if(!datastore.scanExists(ident)){
//				System.out.println("Adding new scan data"+scandata.id+scandata.timestamp);
				double lat = scandata.latitude;
				double lon = scandata.longitude;
				double[] absoluteEdges = new double[360 * 2];
//				System.out.println(scandata.distanceReadings.length);
				for (int i = 0; i < 360; i ++) {
				    absoluteEdges[i*2] = lat + mToD(scandata.distanceReadings[i] * Math.sin(Math.toRadians(i)));
				    absoluteEdges[i*2+1] = lon + mToD(scandata.distanceReadings[i] * Math.cos(Math.toRadians(i)));
				}
				Scan scan = new Scan(scandata.latitude, scandata.longitude, scandata.depth, scandata.flowRate, absoluteEdges);
				datastore.addScan(ident.replace(":", "").replace(".", ""), scan); //because jqyuery hates colons and periods in selectors.
			}
		} else if(Message.getType(data) == PathData.class){
			PathData pathdata = new PathData(data);
			System.out.println("Got eta of "+pathdata.eta+" from drone "+pathdata.id);
			if(pathdata.pathCommandID.equals(datastore.getSearchArea().id)){
				System.out.println("Got eta");
				if(!datastore.getSearchArea().etas.containsKey(pathdata.id)){
					System.out.println("New ETA! Adding.");
					datastore.getSearchArea().addEta(pathdata.id, pathdata.eta);
				}
				
			}
		}
		//TODO - should this be moved?
		if( Message.getType(data)==ScanData.class){
//			System.out.println("Acking scan data");
			Acknowledgement ack = new ScanAcknowledgement(id, dt);
//			System.out.println(ack.toString());
			Broadcast.broadcast(ack.toString());
		}
	}
	public static double mToD(double m) {
		double deg = Math.toDegrees(m / EARTH_RADIUS);
		return deg;
	}

}
