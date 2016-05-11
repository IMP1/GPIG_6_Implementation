package droneserver;

import java.io.IOException;
import java.io.Reader;
import java.net.Socket;
import java.util.Date;

import datastore.Datastore;
import datastore.Drone;
import datastore.Scan;
import handler.Handler;
import network.Command;
import network.Message.Type;
import network.Report;
import network.ScanData;

public class DroneServerHandler extends Handler {

	public DroneServerHandler(Socket socket, Datastore datastore) throws IOException {
		super(socket, datastore);
	}
	
	@Override
	public void run() {
		System.out.println("Drone Request Received, handler started");
		try {
			String message = reader.readLine();
			//if type info
			if(Command.getType(message)==Type.INFO){
				Report report = new Report(message);
				//if we've seen this drone before, update it
				if(datastore.droneExists(report.id)){
					Drone drone = datastore.getDroneById(report.id);
					Long timestamp = Long.parseLong(report.timestamp);
					Date messagedate = new Date(timestamp);
					Long storedtimestamp = Long.parseLong(drone.getTimestamp());
					Date storeddate = new Date(storedtimestamp);
					if(messagedate.after(storeddate)){
						System.out.println("Updating known drone "+report.id);
						drone.setTimestamp(report.timestamp);
						drone.setBatteryLevel(report.batteryStatus);
						drone.setLocLat(report.latitude);
						drone.setLocLong(report.longitude);
						drone.setStatus(report.status);
					}
					
				}else{
					//otherwise create it and chuck it in the datastore.
					System.out.println("Creating new drone "+report.id);
					Drone drone = new Drone(report.batteryStatus, report.latitude, report.longitude, report.status, report.timestamp);
					datastore.addDrone(report.id, drone);
				}
			} else if (Command.getType(message)== Type.SCAN_DATA){
				ScanData scandata = new ScanData(message);
				System.out.println("Adding new scan data"+scandata.id+scandata.timestamp);
				String id = scandata.id+scandata.timestamp;
				if(!datastore.scanExists(id)){
					Scan scan = new Scan(scandata.latitude, scandata.longitude, scandata.depth, scandata.flowRate, scandata.distanceReadings);
					datastore.addScan(id, scan);
				}
			}
			//TODO ACK.
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//Close the socket.
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
