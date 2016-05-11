package network;

public abstract class Message {

	public static final String MESH_GROUP_ADDRESS = "234.0.0.6";
	public static final int MESH_PORT = 5224;
	public static final int PACKAGE_SIZE = 1024;
	
	protected final static String SEPARATOR = ";";
	
	public enum Type {
		INFO, COMMAND, SCAN_DATA;
	}
	
	public static Type getType(String message) {
		message = setup(null, message);
		if (message.startsWith(Command.PREFIX)) {
			return Type.COMMAND;
		} else if (message.startsWith(Report.PREFIX)) {
			return Type.INFO;
		} else if (message.startsWith(ScanData.PREFIX)) {
			return Type.SCAN_DATA;
		} else {
			throw new RuntimeException("This isn't a support message type: " + message);
		} 
	}
	
	public String id;
	public String timestamp;
	
	Message(String id, String timestamp) {
		this.id = id;
		this.timestamp = timestamp;
	}
	
	Message() {}
	
	static String setup(Message msgObj, String message) {
		String data[] = message.split(SEPARATOR);
		if (data.length < 2) throw new RuntimeException("No ID or Timestamp given.");
		if (msgObj != null) {
			msgObj.id = data[0];
			msgObj.timestamp = data[1];
		}
		int metadataOffset = message.indexOf(SEPARATOR, message.indexOf(SEPARATOR) + 1) + 1; // second separator's index
		return message.substring(metadataOffset);
	}
	
}
