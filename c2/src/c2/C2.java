package c2;

import datastore.Datastore;
import droneserver.DroneServer;
import frontendserver.FrontendServer;

public class C2 {
	public static final int FRONTEND_PORT = 4040;
	public static final int DRONES_PORT = 4041;
	public static Datastore datastore = new Datastore();

	public static void main(String[] args) {
		System.out.println("C2 System Starting");
		C2.start();
	}
	
	public static void start(){
		DroneServer droneserver = new DroneServer(DRONES_PORT, datastore);
		new Thread(droneserver).start();
		FrontendServer frontendserver = new FrontendServer(FRONTEND_PORT, datastore);
		new Thread(frontendserver).start();
	}

}
