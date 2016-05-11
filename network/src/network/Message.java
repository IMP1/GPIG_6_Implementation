package network;

public abstract class Message {

	public enum Type {
		INFO, COMMAND, SCAN_DATA;
	}
	
	public Type getType(String message) {
		if (message.startsWith(Command.PREFIX)) {
			return Type.COMMAND;
		} else if (message.startsWith(Report.PREFIX)) {
			return Type.INFO;
		} else if (message.startsWith(ScanData.PREFIX)) {
			return Type.SCAN_DATA;
		} else {
			throw new RuntimeException("This isn't a support message type.");
		} 
	}
	
}
