package drones.mesh;

import network.Command;
import network.Data;
import network.Message;
import network.MoveCommand;
import network.PathCommand;
import network.ScanAcknowledgement;
import network.ScanData;
import drones.Drone;

public class MessageHandler {
	
	private final MeshNetworkingThread networkingThread;
	
	protected MessageHandler(MeshNetworkingThread networkingThread) {
		this.networkingThread = networkingThread;
	}

	/**
	 * Handles a message recieved by this drone.
	 * If it has already been recieved and dealt with, it is ignored.
	 * If it is a path command, we return our estimate.
	 * If it is destined for another drone, we pass it along, updating our 
	 * map with the information if possible.
	 * If it is destined for us, we handle the command or acknowledgement.
	 * 
	 * Recieving a message also signifies that we have regained connection
	 * to the mesh, and so this triggers a resend of all the stored messages.
	 * @param message
	 */
	protected void handleMessage(String message) {
		if (!Message.getId(message).equals(Drone.ID)) {
			// We're reconnected! (In theory)
			networkingThread.resendAllStoredMessages(); 
		}
		if (networkingThread.isMessageDealtWith(message)) {
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
			networkingThread.sendMessage(message);
			networkingThread.addDealtWithMessage(message);
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
			networkingThread.addDealtWithMessage(message);
			System.out.println("Handling Move Command.");
		} else if (PathCommand.class.isAssignableFrom(Message.getType(message))) {
			PathCommand command = new PathCommand(message);
			Drone.mesh().addCommand(command);
			networkingThread.addDealtWithMessage(message);
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
		networkingThread.acknowledgeMessage(ack.timestamp);
		networkingThread.addDealtWithMessage(message);
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
		networkingThread.addDealtWithMessage(message);
	}
	
	
}
