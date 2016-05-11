package drones.mesh;

import drones.routing.RoutingHandler;

public class MeshInterfaceThread extends Thread {

	private RoutingHandler router;
	private MeshNetworkingThread networkingThread;
	
	public MeshInterfaceThread() {
		router = new RoutingHandler();
		networkingThread = new MeshNetworkingThread();
		networkingThread.start();
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
		router.calculate(latitude, longitude, radius);
		//TODO: call calculate(point, radius)
		
	}
	
	private void requestRouteNavigation() {
		//TODO: call go(point, radius)
	}
	
}
