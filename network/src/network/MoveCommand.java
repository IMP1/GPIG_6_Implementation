package network;

public class MoveCommand extends Command {
	
	public final static String MOVE_COMMAND_PREFIX = "MOVE";
	
	public final String latitude;
	public final String longitude;
	public final String radius;
	
	public MoveCommand(String id, String timestamp, String latitude, String longitude, String radius) {
		super(id, timestamp);
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(MOVE_COMMAND_PREFIX); sb.append(SEPARATOR);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(radius);
		return sb.toString();
	}
	
	public MoveCommand(final String rawMessage) {
		super(rawMessage);
		final String commandMessage = Command.strip(rawMessage);
		if (!commandMessage.startsWith(MOVE_COMMAND_PREFIX)) throw new RuntimeException("A Command Type {PATH, MOVE} needs to be supplied.");
		final String moveMessage = commandMessage.substring(MOVE_COMMAND_PREFIX.length() + 1);
		String data[] = moveMessage.split(SEPARATOR);
		if (data.length != 3) {
			System.err.println(rawMessage);
			throw new RuntimeException("A MOVE Command Message must have 3 arguments: latitude, longitude, radius.");
		}
		latitude = data[0];
		longitude = data[1];
		radius = data[2];
	}

}
