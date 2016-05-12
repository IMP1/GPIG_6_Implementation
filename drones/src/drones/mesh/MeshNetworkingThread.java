package drones.mesh;

import java.util.ArrayList;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Mesh Networking Thread
 * Recieves messages being broadcast, and handles its
 * connection to the network, as well as sending
 * packets across the mesh from and to the C2. 
 * 
 * @author Huw Taylor
 *
 */
public class MeshNetworkingThread extends Thread {
	
	private ArrayList<network.Message> unacknowledgedSentMessages;
	private ArrayList<network.Message> sentMessages; //TODO: store as hashes for quicker lookup?
	
	private MulticastSocket socket;
	private InetAddress groupAddress;
	
	/**
	 * Constructor called from MeshInterfaceThread. Initialises sockets.
	 */
	protected MeshNetworkingThread() {
		try {
    		groupAddress = InetAddress.getByName(network.Message.MESH_GROUP_ADDRESS);
    		socket = new MulticastSocket(network.Message.MESH_PORT);
    		socket.joinGroup(groupAddress);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Well, shit. :(");
		}
	}

	/**
	 * Listens for any broadcasts to the group, and calls 
	 * {@link #recieveMessage(String)} with the received message.
	 */
	@Override
	public void run() {
		while (true) {
			try {
				byte[] receiveData = new byte[network.Message.PACKAGE_SIZE];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				socket.receive(receivePacket);
				String message = new String(receivePacket.getData());
				recieveMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Resends all unacknowledged messages.
	 * This is intended to be used when connection to the mesh
	 * is lost and then reestablished.
	 */
	public void resendAllStoredMessages() {
		for (network.Message message : unacknowledgedSentMessages) {
			broadcastData(message);
		}
	}
	
	private void broadcastData(network.Message message) {
		sendMessage(message.toString());
		sentMessages.add(message);
	}
	
	private void recieveMessage(String message) {
		System.out.println("Receieved message: " + message);
		//TODO: resend if not bound for here, and not already sent from here.
		//TODO: handle acknowledgments
		//TODO: interpret commands from the C2
		
	}

	private void sendMessage(String message) {
		try {
			byte[] data = message.getBytes();
			if (data.length > network.Message.PACKAGE_SIZE) {
				System.err.printf("THIS PACKAGE IS %d BYTES LONG.\nTHIS WILL BE TOO BIG TO BE READ.\nTHE MAX IS CURRENTLY %d\n", 
								  data.length, network.Message.PACKAGE_SIZE);
			}
			DatagramPacket packet = new DatagramPacket(data, data.length, groupAddress, network.Message.MESH_PORT);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
