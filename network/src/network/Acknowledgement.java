package network;

public class Acknowledgement extends Message {

	protected final static String PREFIX = "ACK";
	
	public Acknowledgement(String id, String timestamp) {
		super(id, timestamp);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id); sb.append(SEPARATOR);
		sb.append(timestamp); 
		sb.append("\n");
		return sb.toString();
	}
	
	public Acknowledgement(String message) {
		message = setup(this, message);
		if (!message.startsWith(PREFIX)) throw new RuntimeException();
	}
	
}
