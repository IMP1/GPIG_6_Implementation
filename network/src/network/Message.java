package network;

import java.time.LocalDateTime;

/**
 * Abstract Message class. Subclasses are used for packing and unpacking data strings.
 * This is shared across the C2 and Drone projects and used to ensure data is in the 
 * expected format.
 *  
 * @author Huw Taylor and Alex Petherick
 */
public abstract class Message {

	public static final String MESH_GROUP_ADDRESS = "234.0.0.6";
	public static final int MESH_PORT = 5224;
	public static final int PACKAGE_SIZE = 1024;
	
	protected final static String SEPARATOR = ";";
	
	public static Class<? extends Message> getType(final String rawMessage) {
		final String message = Message.strip(rawMessage);
		if (message.startsWith(Command.COMMAND_PREFIX)) {
			final String commandMessage = Command.strip(rawMessage);
			if (commandMessage.startsWith(MoveCommand.MOVE_COMMAND_PREFIX)) {
				return MoveCommand.class;
			} else if (message.startsWith(PathCommand.COMMAND_PREFIX)) {
				return PathCommand.class;
			} else {
				throw new RuntimeException("This isn't a supported message type: " + commandMessage + ".\nMust be either COMMAND or DATA.");
			}
		} else if (message.startsWith(Data.DATA_PREFIX)) {
			final String dataMessage = Data.strip(rawMessage);
			if (dataMessage.startsWith(ScanData.SCAN_DATA_PREFIX)) {
				return ScanData.class;
			} else if (dataMessage.startsWith(PathData.PATH_DATA_PREFIX)) {
				return PathData.class;
			} else if (dataMessage.startsWith(StatusData.STATUS_DATA_PREFIX)) {
				return StatusData.class;
			} else {
				throw new RuntimeException("This isn't a supported message type: " + dataMessage + ".\nMust be either COMMAND or DATA.");
			}
		} else if (message.startsWith(Acknowledgement .ACKNOWLEDGEMENT_PREFIX)) {
			return Acknowledgement.class;
		}else{
			throw new RuntimeException("This isn't a supported message type: " + message + ".\nMust be either COMMAND or DATA.");
		}
	}
	
	public static String getId(final String rawMessage) {
		return rawMessage.split(SEPARATOR)[0];
	}
	
	public static LocalDateTime getTimestamp(final String rawMessage) {
		return LocalDateTime.parse(rawMessage.split(SEPARATOR)[1]);
	}
	
	public final String id;
	public final LocalDateTime timestamp;
	
	protected Message(String id, LocalDateTime timestamp) {
		this.id = id;
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id); sb.append(SEPARATOR);
		sb.append(timestamp); sb.append(SEPARATOR);
		return sb.toString();
	}
	
	protected Message(String message) {
		String data[] = message.split(SEPARATOR);
		if (data.length < 2) throw new RuntimeException("Both the Drone ID and the Timestamp need to be supplied.");
		id = data[0];
		timestamp = LocalDateTime.parse(data[1]);
	}
	
	protected static String strip(String message) {
		int firstSeparatorIndex = message.indexOf(SEPARATOR);
		int secondSeparatorIndex = message.indexOf(SEPARATOR, firstSeparatorIndex + 1);
		String strippedMessage = message.substring(secondSeparatorIndex + 1);
		return strippedMessage;
	}
	
}
