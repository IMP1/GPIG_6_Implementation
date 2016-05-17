package drones.mesh;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import network.Command;
import network.PathCommand;
import network.MoveCommand;
import network.PathData;
import network.ScanData;
import network.StatusData;
import network.StatusData.DroneState;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.PointList;

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
	
	public final static int MILLISECOND_TICK_DELAY = 250;
	public final static int MILLISECOND_TICK_DELAY_LOW_BATTERY = 1000;

	private MeshNetworkingThread networkingThread = null;
	private RoutingHandler router = null;
	private HashMap<String, Future<PathWrapper>> paths = new HashMap<>();
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
			drones.MapHelper.addScan(scan); //TODO: check to see if this is done elsewhere. 
		}
		//TODO: Check to see if the drone is full up of scan. 
		//      If so, maybe set state to returning.
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
				if (isBatteryTooLow()) {
					Thread.sleep(MILLISECOND_TICK_DELAY_LOW_BATTERY);
				} else {
					Thread.sleep(MILLISECOND_TICK_DELAY);
				}
			} catch (InterruptedException e) { e.printStackTrace(); /* oh no(!) */ }
		}
	}
	
	private void tick() {
		for (String commandID : paths.keySet()) {
			if (paths.get(commandID).isDone()) {
				try {
					broadcastRouteData(commandID, paths.get(commandID).get());
					paths.remove(commandID);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		for (Command command : getCommands()) {
			if (command instanceof PathCommand) {
				PathCommand pathCommand = (PathCommand)command;
				if (Drone.state() != DroneState.IDLE) {
					broadcastUndoableRoute(pathCommand.id);
				} else {
					requestRouteCalculation(pathCommand.id, pathCommand.latitude, pathCommand.longitude, 0);
				}
			} else if (command instanceof MoveCommand) {
				MoveCommand moveCommand = (MoveCommand)command;
				requestRouteNavigation(moveCommand.latitude, moveCommand.longitude, moveCommand.radius);
			}
		}

		for (ScanData scan : getScans()) {
			LocalDateTime timestamp = java.time.LocalDateTime.now();
			network.ScanData scanData = new network.ScanData(Drone.ID, timestamp, scan.latitude, scan.longitude, scan.depth, scan.flowRate, scan.distanceReadings);
			networkingThread.sendMessage(scanData, true); 
		}
		
		handleBatteryLevel();
		
		sendCurrentState();
	}
	
	private void handleBatteryLevel() {
		if (isBatteryTooLow()) {
			Drone.setState(DroneState.RETURNING);
		}
	}
	
	private boolean isBatteryTooLow() {
		//XXX: some function of distance? 
		return SensorInterface.getBatteryLevel() < 0.4;
	}
	
	private void sendCurrentState() {
		final String id = Drone.ID;
		final LocalDateTime time = LocalDateTime.now();
		final double lat = SensorInterface.getGPSLatitude();
		final double lon = SensorInterface.getGPSLongitude();
		final double batteryLevel = SensorInterface.getBatteryLevel();
		final DroneState state = Drone.state();
		final PointList currentPath = Drone.nav().getCurrentPath();
		final double[] path;
		if (currentPath != null) {
			final int n = Drone.nav().getCurrentPath().getSize();
			path = new double[n * 2];
			for (int i = 0; i < n; i += 2) {
				path[i/2] = currentPath.getLatitude(i/2);
				path[i/2 + 1] = currentPath.getLatitude(i/2);
			}
		} else {
			path = new double[0];
		}
		StatusData currentState = new StatusData(id, time, lat, lon, batteryLevel, state, path);
		networkingThread.sendMessage(currentState, false);
	}
	
	private void broadcastRouteData(String commandID, PathWrapper calculatedPath) {
		LocalDateTime timestamp = java.time.LocalDateTime.now();
		PathData pathMessage = new PathData(Drone.ID, timestamp, commandID, calculatedPath.getDistance());
		networkingThread.sendMessage(pathMessage, false);
	}
	
	private void broadcastUndoableRoute(String commandID) {
		LocalDateTime timestamp = java.time.LocalDateTime.now();
		PathData pathMessage = new PathData(Drone.ID, timestamp, commandID, Double.MAX_VALUE);
		networkingThread.sendMessage(pathMessage, false);
	}
	
	/**
	 * Adds scan data from another drone's broadcast to this drone's local map.
	 * @param scanData another drone's scan data
	 */
	protected void addExternalScanData(ScanData scan) {
		drones.MapHelper.addScan(scan);
	}
	
	private void requestRouteCalculation(String commandID, double latitude, double longitude, double radius) {
		paths.put(commandID, router.calculate(latitude, longitude, radius));
	}
	
	private void requestRouteNavigation(double latitude, double longitude, double radius) {
		router.go(latitude, longitude, radius);
		//TODO: make sure router changes the drone's state to moving / searching when necessary
	}
	
}
