package drones.mesh;

import network.Command;
import network.Data;
import network.Message;
import network.MoveCommand;
import network.PathCommand;
import network.ScanAcknowledgement;
import network.ScanData;
import network.StatusData;
import drones.Drone;

import static drones.mesh.MeshNetworkingThread.DEBUG_MESSAGES;

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
	 * 
	 * <pre>PathCommand.class.isAssignableFrom(messageClass)</pre> is the
	 * silly java way of returning whether {@link messageClass} is a subclass
	 * of {@link PathCommand}.
	 * 
	 * @param message
	 */
	protected void handleMessage(String message) {
		if (networkingThread.isMessageDealtWith(message)) {
			if (DEBUG_MESSAGES) System.out.println("[Mesh Network] Duplicate. Ignoring.");	
			return;
		}
		if (DEBUG_MESSAGES) System.out.println("[Mesh Network] Not a duplicate...");
		final Class<? extends Message> messageClass = Message.getType(message);
		if (PathCommand.class.isAssignableFrom(messageClass)) {
			handleCommand(message);
		}
		if (Message.getId(message).equals(Drone.ID)) { // if it's for/from us
			if (Command.class.isAssignableFrom(messageClass)) {
				handleCommand(message);
			} else if (ScanAcknowledgement.class.isAssignableFrom(messageClass)) {
				handleScanAcknowledgement(message);
			} else {
				if (DEBUG_MESSAGES) System.out.printf("[Mesh Network] Receieved data from ourself. Ignoring.\n");
			}
		} else {
			if (DEBUG_MESSAGES) System.out.println("[Mesh Network] Not for us. Passing along.");
			if (Data.class.isAssignableFrom(messageClass)) {
				handleOtherData(message);
			}
			// Messages are just rebroadcast across the mesh.
			// * Commands for other drones
			// * Acks from other drones
			networkingThread.addDealtWithMessage(message);
			networkingThread.sendMessage(message);
		}
		if (!Message.getId(message).equals(Drone.ID)) {
			// We're reconnected! (In theory)
			if (DEBUG_MESSAGES) System.out.println("[Mesh Network] Recieved a message from someone else. Resending stored messages...");
			networkingThread.resendAllStoredMessages();
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
			if (DEBUG_MESSAGES) System.out.println("[Mesh Network] Handling Move Command.");
		} else if (PathCommand.class.isAssignableFrom(Message.getType(message))) {
			PathCommand command = new PathCommand(message);
			Drone.mesh().addCommand(command);
			networkingThread.addDealtWithMessage(message);
			if (DEBUG_MESSAGES) System.out.println("[Mesh Network] Handling Path Command.");
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
		if (DEBUG_MESSAGES) System.out.printf("[Mesh Network] Receieved data from Drone %s.\n", Message.getId(message));
		if (ScanData.class.isAssignableFrom(Message.getType(message))) {
			ScanData data = new ScanData(message);
			Drone.mesh().addExternalScanData(data);
		}
		if (StatusData.class.isAssignableFrom(Message.getType(message))) {
			StatusData data = new StatusData(message);
			Drone.mesh().addExternalPosition(data);
		}
		networkingThread.addDealtWithMessage(message);
	}
	
	
}
