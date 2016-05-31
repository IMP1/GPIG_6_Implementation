package c2;

import datastore.Datastore;
import droneserver.DroneServer;
import externalPoll.ExternalPollThread;
import frontendserver.FrontendServer;
import movement.MovementThread;
import network.Message;

public class C2 {
	public static final int FRONTEND_PORT = 8081;
	public static Datastore datastore = new Datastore();

	public static void main(String[] args) {
		System.out.println("C2 System Starting");
		C2.start();
	}
	
	public static void start(){
		DroneServer droneserver = new DroneServer(Message.MESH_PORT, datastore);
		new Thread(droneserver).start();
		FrontendServer frontendserver = new FrontendServer(FRONTEND_PORT, datastore);
		new Thread(frontendserver).start();
		ExternalPollThread externalpoll = new ExternalPollThread(datastore);
		new Thread(externalpoll).start();
		MovementThread movement = new MovementThread(datastore);
		new Thread(movement).start();
	}

}
