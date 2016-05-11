package droneserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import datastore.Datastore;

public class DroneServer implements Runnable {
	
	private final int port;
	private Datastore datastore;
	private MulticastSocket serverSocket;

	public DroneServer(int port, Datastore datastore) {
		this.port = port;
		this.datastore = datastore;
	}

	@Override
	public void run() {
		System.out.println("Drone server starting on port "+port);
		try {
			serverSocket = new MulticastSocket(port);
			serverSocket.joinGroup(InetAddress.getByName("234.0.0.6"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] receiveData = new byte[1024];
		while(true){
			try {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);  
				serverSocket.receive(receivePacket);
				String sentence = new String( receivePacket.getData());
				DroneServerHandler handler = new DroneServerHandler(sentence, datastore);
				new Thread(handler).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}

}
