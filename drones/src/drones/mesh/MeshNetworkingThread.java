package drones.mesh;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import drones.Drone;

import network.*;

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
		//TODO: remove testing
		String id = Drone.ID;
		LocalDateTime timestamp = LocalDateTime.now();
		double latitude = 53.955391;
		double longitude = -1.078967; 
		double radius = 10.0;
		PathCommand p = new PathCommand(id, timestamp, latitude, longitude, radius);
		recieveMessage(p.toString());
		//TODO: remove testing
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
	protected void resendAllStoredMessages() {
		for (network.Message message : unacknowledgedSentMessages) {
			broadcastData(message);
		}
	}
	
	private void broadcastData(network.Message message) {
		sendMessage(message.toString());
		sentMessages.add(message);
	}
	
	private void recieveMessage(String message) {
		System.out.printf("Receieved message: '%s'.\n", message);
		if (Message.getId(message).equals(Drone.ID)) {
			Class<? extends Message> messageClass = Message.getType(message); 
			if (Command.class.isAssignableFrom(messageClass)) {
				handleCommand(message);
			} else if (Acknowledgement.class.isAssignableFrom(messageClass)) {
				handleAcknowledgement(message);
			}
		} else {
			//TODO: resend if not bound for here, and not already sent from here.
		}
	}
	
	private void handleCommand(String message) {
		if (MoveCommand.class.isAssignableFrom(Message.getType(message))) {
			MoveCommand command = new MoveCommand(message);
			Drone.mesh().addCommand(command);
		} else if (PathCommand.class.isAssignableFrom(Message.getType(message))) {
			PathCommand command = new PathCommand(message);
			Drone.mesh().addCommand(command);
		}
	}
	
	private void handleAcknowledgement(String message) {
		
	}
	
	/**
	 * Sends the string representation of this Message object across the mesh network.
	 * @param message the message object to be sent.
	 */
	protected void sendMessage(network.Message message) {
		sendMessage(message.toString());
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
