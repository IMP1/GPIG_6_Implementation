package drones.mesh;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import network.Command;
import network.PathCommand;
import network.MoveCommand;
import network.PathData;
import network.ScanData;
import network.StatusData;

import com.graphhopper.PathWrapper;

import drones.Drone;
import drones.routing.RoutingHandler; 
import drones.sensors.SensorInterface;

/**
 * Mesh Interface Thread
 * The interface of the Drone for the mesh. 
 * Handles networking with the mesh, and handles commands given, 
 * calling out to the drone's routing subsystem.
 *  
 * @author Huw Taylor
 */
public class MeshInterfaceThread extends Thread {
	
	public final static int NANOSECOND_TICK_DELAY = 100;

	private MeshNetworkingThread networkingThread = null;
	private RoutingHandler router = null;
	private Future<PathWrapper> path = null;
	private ArrayList<Command> commandBuffer = new ArrayList<>();
	private ArrayList<ScanData> scanBuffer = new ArrayList<>();

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
	 * @param scan a wrapper around a set of numeric values.
	 */
	public void addScan(ScanData scan) {
		synchronized (scanBuffer) {
			scanBuffer.add(scan);
		}
	}
	
	/**
	 * Retuns the list of scans added externally by the scanner so far.
	 * Is thread safe with respect to itself, and adding scans to the 
	 * scan data buffer.
	 * @return a list of ScanData objects.
	 */
	private ScanData[] getScans() {
		synchronized (scanBuffer) {
			ScanData[] scans = scanBuffer.toArray(new ScanData[scanBuffer.size()]);
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
				Thread.sleep(NANOSECOND_TICK_DELAY);
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
				requestRouteCalculation(pathCommand.latitude, pathCommand.longitude, 0);
			} else if (command instanceof MoveCommand) {
				MoveCommand moveCommand = (MoveCommand)command;
				requestRouteNavigation(moveCommand.latitude, moveCommand.longitude, moveCommand.radius);
			}
		}

		for (ScanData scan : getScans()) {
			LocalDateTime timestamp = java.time.LocalDateTime.now();
			network.ScanData scanData = new network.ScanData(Drone.ID, timestamp, scan.latitude, scan.longitude, scan.depth, scan.flowRate, scan.distanceReadings);
			networkingThread.sendMessage(scanData); 
		}
		
		handleBatteryLevel();
		
		sendCurrentState();
	}
	
	private void handleBatteryLevel() {
		// TODO handle battery stuff
		//      if it's too low, oh no!
		// TODO make sure drone.sensors has some way of giving battery level (random number)
	}
	
	private void sendCurrentState() {
		final String id = Drone.ID;
		final LocalDateTime time = LocalDateTime.now();
		final double lat = SensorInterface.getGPSLatitude();
		final double lon = SensorInterface.getGPSLongitude();
		final double batteryLevel = SensorInterface.getBatteryLevel();
		final String state = "TOTALLY FINE";
		// final double[] currentPath = Drone.nav().getCurrentPath();
		final double[] currentPath = new double[] { 0, 0, 0, 0 };
		StatusData currentState = new StatusData(id, time, lat, lon, batteryLevel, state, currentPath);
		networkingThread.sendMessage(currentState);
	}
	
	private void broadcastRouteData() {
		try {
			final PathWrapper currentPath = path.get();
			LocalDateTime timestamp = java.time.LocalDateTime.now();
			PathData pathMessage = new PathData(Drone.ID, timestamp, currentPath.getDistance());
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
	protected void addExternalScanData(ScanData scan) {
		drones.MapHelper.addScan(scan);
	}
	
	private void requestRouteCalculation(double latitude, double longitude, double radius) {
		path = router.calculate(latitude, longitude, radius);
	}
	
	private void requestRouteNavigation(double latitude, double longitude, double radius) {
		router.go(latitude, longitude, radius);
	}
	
}
