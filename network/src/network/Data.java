package network;

public abstract class Data extends Message {

	public final static String DATA_PREFIX = "DATA";
	
	protected Data(String id, java.time.LocalDateTime timestamp) {
		super(id, timestamp);
	}
	
	protected Data(String message) {
		super(message);
	}
	
	protected static String strip(String message) {
		message = Message.strip(message);
		String[] data = message.split(SEPARATOR);
		if (data.length < 1) throw new RuntimeException("A Message Type {DATA, COMMAND, ACK} needs to be supplied.");
		int separatorIndex = message.indexOf(SEPARATOR);
		return message.substring(separatorIndex + 1);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()); sb.append(SEPARATOR);
		sb.append(DATA_PREFIX);
		return sb.toString();
	}
	
}
