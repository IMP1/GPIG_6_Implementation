package network;

public abstract class Acknowledgement extends Message {

	public final static String ACKNOWLEDGEMENT_PREFIX = "ACK";
	
	protected Acknowledgement(String id, String timestamp) {
		super(id, timestamp);
	}
	
	protected Acknowledgement(String message) {
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
		sb.append(super.toString());
		sb.append(ACKNOWLEDGEMENT_PREFIX); sb.append(SEPARATOR);
		return sb.toString();
	}
	
}
