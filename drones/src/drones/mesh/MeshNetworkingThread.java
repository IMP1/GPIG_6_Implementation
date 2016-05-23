package drones.mesh;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

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
	
	public final static boolean DEBUG_MESSAGES = false; 
	
	private ArrayList<Message> unacknowledgedMessages = new ArrayList<Message>();
	private ArrayList<String> dealtWithMessages = new ArrayList<String>();
	
	private MulticastSocket socket;
	private InetAddress groupAddress;
	private MessageHandler messageHandler;
	
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
	
	protected void addDealtWithMessage(String m) {
		synchronized (dealtWithMessages) {
			dealtWithMessages.add(m);
		}
	}
	
	protected boolean isMessageDealtWith(String m) {
		synchronized (dealtWithMessages) {
			return dealtWithMessages.contains(m);
		}
	}
	
	/**
	 * Constructor called from MeshInterfaceThread. 
	 * Initialises sockets and joins the relevant multicast group.
	 */
	protected MeshNetworkingThread() {
		messageHandler = new MessageHandler(this);
		try {
    		groupAddress = InetAddress.getByName(Message.MESH_GROUP_ADDRESS);
    		socket = new MulticastSocket(Message.MESH_PORT);
    		socket.joinGroup(groupAddress);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("[Mesh Network] Well, shit. :(");
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
				if (DEBUG_MESSAGES) System.out.printf("[Mesh Network] []<-- '%s'\n", message);
				messageHandler.handleMessage(message);
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

	protected void sendMessage(String message) {
		try {
			byte[] data = message.getBytes();
			if (DEBUG_MESSAGES) System.out.printf("[Mesh Network] []--> '%s'\n", message);
			if (data.length > network.Message.PACKAGE_SIZE) {
				System.err.printf("[Mesh Network] THIS PACKAGE IS %d BYTES LONG.\nTHIS WILL BE TOO BIG TO BE READ.\nTHE MAX IS CURRENTLY %d.\n", 
								  data.length, network.Message.PACKAGE_SIZE);
			}
			DatagramPacket packet = new DatagramPacket(data, data.length, groupAddress, network.Message.MESH_PORT);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void acknowledgeMessage(LocalDateTime timestamp) {
		synchronized (unacknowledgedMessages) {
			if (DEBUG_MESSAGES) System.out.println("[Mesh Network] Acknowledging Message...");
			for (int i = unacknowledgedMessages.size() - 1; i >= 0; i --) {
				if (unacknowledgedMessages.get(i).timestamp.equals(timestamp)) {
					unacknowledgedMessages.remove(i);
				}
			}
		}
	}
	
}
