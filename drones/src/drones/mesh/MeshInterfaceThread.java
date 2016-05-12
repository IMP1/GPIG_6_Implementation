package drones.mesh;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.graphhopper.PathWrapper;

import drones.Drone;
import drones.routing.RoutingHandler;
/*TODO import ScannerHandler.Scan; */ 

public class MeshInterfaceThread extends Thread {

	private MeshNetworkingThread networkingThread = null;
	private RoutingHandler router = null;
	private Future<PathWrapper> calculatedPath = null;
	private ArrayList<String> commandBuffer = new ArrayList<String>();
	private ArrayList<Object /*TODO Scan */> scanBuffer = new ArrayList<>();
	
	public void addScan(Object /*TODO Scan */ scan) {
		synchronized (scanBuffer) {
			scanBuffer.add(scan);
		}
	}
	
	private Object/*TODO Scan*/[] getScans() {
		synchronized (scanBuffer) {
			Object/*TODO Scan*/[] scans = scanBuffer.toArray(new Object/*TODO Scan*/[scanBuffer.size()]);
			scanBuffer.clear();
			return scans;
		}
	}
	
	protected void addCommand(String command) {
		synchronized (commandBuffer) {
			commandBuffer.add(command);
		}
	}
	
	private String[] getCommands() {
		synchronized (commandBuffer) {
			String[] commands = commandBuffer.toArray(new String[commandBuffer.size()]);
			commandBuffer.clear();
			return commands;
		}
	}
	
	public MeshInterfaceThread() {
		router = new RoutingHandler();
		networkingThread = new MeshNetworkingThread();
		networkingThread.start();
		addCommand("ROUTETO;53.955391;-1.078967;10.0");
	}

	@Override
	public void run() {
		while (true) {
			tick();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { /* oh no(!) */ }
		}
	}
	
	private void tick() {
		if (calculatedPath != null && calculatedPath.isDone()) {
			broadcastRouteData();
		}
		for (String command : getCommands()) {
			System.out.printf("Doing '%s'.\n", command);
			String[] data = command.split(";");
			if (data[0].equals("ROUTETO")) {
				requestRouteCalculation(Double.valueOf(data[1]), Double.valueOf(data[2]), Double.valueOf(data[3]));
			}
		}
		for (Object /*TODO Scan*/ scan : getScans()) {
			
		}
	}
	
	private void broadcastRouteData() {
		try {
			String[] points = calculatedPath.get().getPoints().toString().replaceAll("\\(", "").split("\\), ");
			for (String p : points) {
				System.out.println(p);
			}
			String timestamp = java.time.LocalDateTime.now().toString();
			network.PathData pathMessage = new network.PathData(Drone.ID, timestamp, points);
			networkingThread.sendMessage(pathMessage);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		calculatedPath = null;
	}
	
	private void recieveScanInformation(/*TODO: param for scan information*/) {
		//TODO: decide where the buffer is going to be, 
		//TODO: read from the buffer if there's anything there.
	}

	private void updateLocalMap() {
		//TODO: decide where the map is going to be.
		//TODO: write to the drone's centralised map data for the other subsystems to use.
	}
	
	private void requestRouteCalculation(double latitude, double longitude, double radius) {
		calculatedPath = router.calculate(latitude, longitude, radius);
	}
	
	private void requestRouteNavigation(double latitude, double longitude, double radius) {
		router.go(latitude, longitude, radius);
	}
	
}
