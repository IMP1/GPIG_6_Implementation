package drones.mesh;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import network.Command;
import network.PathCommand;
import network.MoveCommand;

import com.graphhopper.PathWrapper;

import drones.Drone;
import drones.routing.RoutingHandler;
import drones.scanner.ScannerHandler.Scan; 

/**
 * Mesh Interface Thread
 * The interface of the Drone for the mesh. 
 * Handles networking with the mesh, and handles commands given, 
 * calling out to the drone's routing subsystem.
 *  
 * @author Huw Taylor
 */
public class MeshInterfaceThread extends Thread {

	private MeshNetworkingThread networkingThread = null;
	private RoutingHandler router = null;
	private Future<PathWrapper> path = null;
	private ArrayList<Command> commandBuffer = new ArrayList<>();
	private ArrayList<Scan> scanBuffer = new ArrayList<>();

	/**
	 * Constructor for the Mesh Interface. 
	 * Initialises the networking thread and routing handler. 
	 */
	public MeshInterfaceThread() {
		router = new RoutingHandler();
		networkingThread = new MeshNetworkingThread();
		networkingThread.start();
	}

	/**
	 * Add a scan to be send to the C2 across the mesh.
	 * @param scan a wrapper around a set of numeric values
	 */
	public void addScan(Scan scan) {
		synchronized (scanBuffer) {
			scanBuffer.add(scan);
		}
	}
	
	private Scan[] getScans() {
		synchronized (scanBuffer) {
			Scan[] scans = scanBuffer.toArray(new Scan[scanBuffer.size()]);
			scanBuffer.clear();
			return scans;
		}
	}
	
	/**
	 * Add a command to a command buffer for this drone to execute. 
	 * Currently only called by the Networking thread after recieving
	 * a command from the C2.
	 * @param command the command for this drone to execute.
	 */
	protected void addCommand(Command command) {
		synchronized (commandBuffer) {
			commandBuffer.add(command);
		}
	}
	
	private Command[] getCommands() {
		synchronized (commandBuffer) {
			Command[] commands = commandBuffer.toArray(new Command[commandBuffer.size()]);
			commandBuffer.clear();
			return commands;
		}
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
		if (path != null && path.isDone()) {
			broadcastRouteData();
		}

		for (Command command : getCommands()) {
			if (command instanceof PathCommand) {
				PathCommand pathCommand = (PathCommand)command;
				requestRouteCalculation(pathCommand.latitude, pathCommand.longitude, pathCommand.radius);
			} else if (command instanceof MoveCommand) {
				MoveCommand moveCommand = (MoveCommand)command;
				requestRouteNavigation(moveCommand.latitude, moveCommand.longitude, moveCommand.radius);
			}
		}

		for (Scan scan : getScans()) {
			LocalDateTime timestamp = java.time.LocalDateTime.now();
			network.ScanData scanData = new network.ScanData(Drone.ID, timestamp, scan.lat, scan.lon, scan.depth, scan.flow, scan.distanceReadings);
			networkingThread.sendMessage(scanData); 
		}
		
		//TODO: get current state (position + battery level from sensors)
	}
	
	private void broadcastRouteData() {
		try {
			final PathWrapper currentPath = path.get();
			double[] points = new double[currentPath.getPoints().size() * 2];
			for (int i = 0; i < points.length - 1; i += 2) {
				points[i] = currentPath.getPoints().getLatitude(i / 2);
				points[i + 1] = currentPath.getPoints().getLongitude(i / 2);
			}
			LocalDateTime timestamp = java.time.LocalDateTime.now();
			network.PathData pathMessage = new network.PathData(Drone.ID, timestamp, points);
			networkingThread.sendMessage(pathMessage);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		path = null;
	}
	
	/**
	 * Adds scan data from another drone's broadcast to this drone's local map.
	 * @param scanData another drone's scan data
	 */
	protected void addExternalScanData(Scan scan) {
		drones.MapHelper.addScan(scan);
	}
	
	private void requestRouteCalculation(double latitude, double longitude, double radius) {
		path = router.calculate(latitude, longitude, radius);
	}
	
	private void requestRouteNavigation(double latitude, double longitude, double radius) {
		router.go(latitude, longitude, radius);
	}
	
}
