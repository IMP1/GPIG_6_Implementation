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
	private ArrayList<String> dealtWithMessages;
	
	private MulticastSocket socket;
	private InetAddress groupAddress;
	
	/**
	 * Constructor called from MeshInterfaceThread. Initialises sockets.
	 */
	protected MeshNetworkingThread() {
		sentMessages = new ArrayList<network.Message>();
		unacknowledgedSentMessages = new ArrayList<network.Message>();
		dealtWithMessages = new ArrayList<String>();
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
		//TODO: Remove Testing:
		// <testing>
		String id = Drone.ID;
		LocalDateTime timestamp = LocalDateTime.now();
		double latitude = 53.955391;
		double longitude = -1.078967;
		PathCommand p = new PathCommand(id, timestamp, latitude, longitude);
		sendMessage(p.toString());
		// </testing>
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
	}
	
	private void recieveMessage(String message) {
		if (dealtWithMessages.contains(message)) {
			System.out.println("DUP");
			System.out.println(message);
			System.out.println("Ignoring.");
			return;
		}
		System.out.println("RECV");
		final Class<? extends Message> messageClass = Message.getType(message);
		if (Message.getId(message).equals(Drone.ID)) {
			if (Command.class.isAssignableFrom(messageClass)) {
				handleCommand(message);
			} else if (Acknowledgement.class.isAssignableFrom(messageClass)) {
				handleAcknowledgement(message);
			} else {
				// Data from this drone (being resent across the mesh)
				//TODO: remove debug
				// <debug>
				System.out.printf("Receieved data from ourself.\n");
				System.out.printf("(Message = '%s')\n", message);
				// </debug>
			}
		} else {
			if (Data.class.isAssignableFrom(messageClass)) {
				handleOtherData(message);
			}
			rebroadcast(message);
		}
	}
	
	private void handleCommand(String message) {
		String droneID = Message.getId(message);
		System.out.printf("Receieved command for Drone %s%s.\n", droneID, droneID.equals(Drone.ID) ? " (That's me!)" : "");
		if (MoveCommand.class.isAssignableFrom(Message.getType(message))) {
			MoveCommand command = new MoveCommand(message);
			Drone.mesh().addCommand(command);
			dealtWithMessages.add(message);
		} else if (PathCommand.class.isAssignableFrom(Message.getType(message))) {
			PathCommand command = new PathCommand(message);
			Drone.mesh().addCommand(command);
			dealtWithMessages.add(message);
		}
	}
	
	private void handleAcknowledgement(String message) {
		//TODO: Handle Acknowledgements:
		//      Check whether this acknowledgement is for us.
		//      If so, remove the corresponding message from the unacknowledged list
		//      If not, rebroadcast it.
	}
	
	/**
	 * Handles data being sent from other drones.
	 * Uses it to update local information and retransmits the message along the mesh. 
	 * @param message
	 */
	private void handleOtherData(String message) {
		System.out.printf("Receieved data from Drone %s.\n", Message.getId(message));
		dealtWithMessages.add(message);
	}
	
	private void rebroadcast(String message) {
		sendMessage(message);
	}
	
	/**
	 * Sends the string representation of this Message object across the mesh network.
	 * @param message the message object to be sent.
	 */
	protected void sendMessage(network.Message message) {
		sendMessage(message.toString());
		sentMessages.add(message);
	}

	private void sendMessage(String message) {
		try {
			byte[] data = message.getBytes();
			System.out.printf("Sending %d bytes of data (out of the current maximum %d).\n", data.length, network.Message.PACKAGE_SIZE);
			if (data.length > network.Message.PACKAGE_SIZE) {
				System.err.printf("THIS PACKAGE IS %d BYTES LONG.\nTHIS WILL BE TOO BIG TO BE READ.\nTHE MAX IS CURRENTLY %d.\n", 
								  data.length, network.Message.PACKAGE_SIZE);
			}
			DatagramPacket packet = new DatagramPacket(data, data.length, groupAddress, network.Message.MESH_PORT);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
