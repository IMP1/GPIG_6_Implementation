package drones.mesh;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
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
	
	private ArrayList<Message> unacknowledgedMessages = new ArrayList<Message>();
	private ArrayList<String> dealtWithMessages = new ArrayList<String>();
	
	private MulticastSocket socket;
	private InetAddress groupAddress;
	
	private Message[] getUnacknowledgedMessages() {
		synchronized (unacknowledgedMessages) {
			return unacknowledgedMessages.toArray(new Message[unacknowledgedMessages.size()]);
		}
	}
	
	private void addUnacknowledgedMessage(Message m) {
		synchronized (unacknowledgedMessages) {
			unacknowledgedMessages.add(m);
		}
	}
	
	private void addDealtWithMessage(String m) {
		synchronized (dealtWithMessages) {
			dealtWithMessages.add(m);
		}
	}
	
	private boolean isMessageDealtWith(String m) {
		synchronized (dealtWithMessages) {
			return dealtWithMessages.contains(m);
		}
	}
	
	/**
	 * Constructor called from MeshInterfaceThread. 
	 * Initialises sockets and joins the relevant multicast group.
	 */
	protected MeshNetworkingThread() {
		try {
    		groupAddress = InetAddress.getByName(Message.MESH_GROUP_ADDRESS);
    		socket = new MulticastSocket(Message.MESH_PORT);
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
	protected void resendAllStoredMessages() {
		for (network.Message message : getUnacknowledgedMessages()) {
			sendMessage(message, false);
		}
	}
	
	private void recieveMessage(String message) {
		System.out.printf("[]<-- '%s'\n", message);
		resendAllStoredMessages(); // We're reconnected! In theory.
		if (isMessageDealtWith(message)) {
			System.out.println("Duplicate. Ignoring.");
			return;
		}
		final Class<? extends Message> messageClass = Message.getType(message);
		if (PathCommand.class.isAssignableFrom(messageClass)) {
			handleCommand(message);
		}
		if (Message.getId(message).equals(Drone.ID)) {
			if (Command.class.isAssignableFrom(messageClass)) {
				handleCommand(message);
			} else if (ScanAcknowledgement.class.isAssignableFrom(messageClass)) {
				handleScanAcknowledgement(message);
			} else {
//				System.out.printf("Receieved data from ourself. Ignoring.\n");
			}
		} else {
			System.out.println("Not for us. Passing along.");
			if (Data.class.isAssignableFrom(messageClass)) {
				handleOtherData(message);
			}
			// Messages are just rebroadcast across the mesh.
			// * Commands for other drones
			// * Acks from other drones
			sendMessage(message);
			addDealtWithMessage(message);
		}
	}
	
	/**
	 * Handles commands for this drone.
	 * Creates the relevant command class, and adds it to the Mesh Interface's buffer.
	 * @param message
	 */
	private void handleCommand(String message) {
		if (MoveCommand.class.isAssignableFrom(Message.getType(message))) {
			MoveCommand command = new MoveCommand(message);
			Drone.mesh().addCommand(command);
			addDealtWithMessage(message);
			System.out.println("Handling Move Command.");
		} else if (PathCommand.class.isAssignableFrom(Message.getType(message))) {
			PathCommand command = new PathCommand(message);
			Drone.mesh().addCommand(command);
			addDealtWithMessage(message);
			System.out.println("Handling Path Command.");
		}
	}
	
	/**
	 * Handles acknowledgements for this drone. 
	 * Removes the message from the list of unacknowledged messages.  
	 * @param ack
	 */
	private void handleScanAcknowledgement(String message) {
		ScanAcknowledgement ack = new ScanAcknowledgement(message); 
		synchronized (unacknowledgedMessages) {
			for (int i = unacknowledgedMessages.size() - 1; i >= 0; i --) {
				if (unacknowledgedMessages.get(i).timestamp.equals(ack.timestamp)) {
					unacknowledgedMessages.remove(i);
				}
			}
		}
		addDealtWithMessage(message);
	}
	
	/**
	 * Handles data being sent from other drones.
	 * Uses it to update local information and retransmits the message along the mesh. 
	 * @param message
	 */
	private void handleOtherData(String message) {
		System.out.printf("Receieved data from Drone %s.\n", Message.getId(message));
		if (ScanData.class.isAssignableFrom(Message.getType(message))) {
			ScanData data = new ScanData(message);
			Drone.mesh().addExternalScanData(data);
		}
		addDealtWithMessage(message);
	}
	
	/**
	 * Sends the string representation of this Message object across the mesh network.
	 * @param message the message object to be sent.
	 */
	protected void sendMessage(network.Message message, boolean needsAcknowledgement) {
		sendMessage(message.toString());
		if (needsAcknowledgement) {
			addUnacknowledgedMessage(message);
		}
	}

	private void sendMessage(String message) {
		try {
			byte[] data = message.getBytes();
			System.out.printf("[]--> '%s'\n", message);
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
