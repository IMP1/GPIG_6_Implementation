package drones.scanner;

import drones.mesh.MeshInterfaceThread;
import drones.scanner.ScannerHandler;

public class ScannerTest {
	private static MeshInterfaceThread meshThread = null;
	public static void main(String[] args){
		// Initialise and begin mesh interface thread
		meshThread = new MeshInterfaceThread();
		meshThread.start();
		System.out.println("Mesh Interface created.");
		testSensorStub();
	}
	
	public static void testSensorStub(){
		Runnable Scanner = new ScannerHandler();
		Scanner.run();
	}

}
