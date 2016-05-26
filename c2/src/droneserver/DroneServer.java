package droneserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;

import datastore.Datastore;
import network.Message;

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
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
			NetworkInterface eth0 = null;
			while (enumeration.hasMoreElements()) {
			    eth0 = enumeration.nextElement();
			    if (eth0.getName().equals("p3p1")) {
			        //there is probably a better way to find ethernet interface
			        break;
			    }
			}
			serverSocket.setNetworkInterface(eth0);
			serverSocket.joinGroup(InetAddress.getByName(Message.MESH_GROUP_ADDRESS));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] receiveData = new byte[Message.PACKAGE_SIZE];
		while(true){
			try {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);  
				serverSocket.receive(receivePacket);
				String sentence = new String( receivePacket.getData());
				DroneServerHandler handler = new DroneServerHandler(sentence, datastore);
				new Thread(handler).start();
				receiveData = new byte[Message.PACKAGE_SIZE];
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}

}
