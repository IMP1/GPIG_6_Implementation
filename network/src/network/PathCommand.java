package network;

public class PathCommand extends Command {
	
	public final static String PATH_COMMAND_PREFIX = "PATH";
	
	public final double latitude;
	public final double longitude;
	
	public PathCommand(String id, java.time.LocalDateTime timestamp, double latitude, double longitude) {
		super(id, timestamp);
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(PATH_COMMAND_PREFIX); sb.append(SEPARATOR);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		return sb.toString();
	}
	
	public PathCommand(final String rawMessage) {
		super(rawMessage);
		final String commandMessage = Command.strip(rawMessage);
		if (!commandMessage.startsWith(PATH_COMMAND_PREFIX)) throw new RuntimeException("A Command Type {PATH, MOVE} needs to be supplied.");
		final String moveMessage = commandMessage.substring(PATH_COMMAND_PREFIX.length() + 1);
		String data[] = moveMessage.split(SEPARATOR);
		if (data.length != 2) {
			System.err.println(rawMessage);
			throw new RuntimeException("A MOVE Command Message must have 3 arguments: latitude, longitude, radius.");
		}
		latitude  = Double.parseDouble(data[0]);
		longitude = Double.parseDouble(data[1]);
	}

}
