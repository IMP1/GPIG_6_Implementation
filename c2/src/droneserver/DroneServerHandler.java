package droneserver;

import java.io.IOException;
import java.net.Socket;

import datastore.Datastore;
import handler.Handler;

public class DroneServerHandler extends Handler {

	public DroneServerHandler(Socket socket, Datastore datastore) throws IOException {
		super(socket, datastore);
	}
	
	@Override
	public void run() {
		System.out.println("Drone Request Received, handler started");
		writer.println("Yo");
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
