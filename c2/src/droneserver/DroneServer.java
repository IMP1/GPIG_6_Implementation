package droneserver;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import datastore.Datastore;

public class DroneServer implements Runnable {
	
	private final int port;
	private Datastore datastore;
	private DatagramSocket serverSocket;

	public DroneServer(int port, Datastore datastore) {
		this.port = port;
		this.datastore = datastore;
	}

	@Override
	public void run() {
		System.out.println("Drone server starting on port "+port);
		try {
			serverSocket = new DatagramSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true){
			try {
				DroneServerHandler handler = new DroneServerHandler(clientSocket, datastore);
				new Thread(handler).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}

}
