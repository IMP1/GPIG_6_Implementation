package network;

public final class MoveCommand extends Command {
	
	public final static String MOVE_COMMAND_PREFIX = "MOVE";
	
	public final double latitude;
	public final double longitude;
	public final double radius;
	
	public MoveCommand(String id, java.time.LocalDateTime timestamp, double latitude, double longitude, double radius) {
		super(id, timestamp);
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()); sb.append(SEPARATOR);
		sb.append(MOVE_COMMAND_PREFIX); sb.append(SEPARATOR);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(radius);
		sb.append(SUFFIX);
		return sb.toString();
	}
	
	public MoveCommand(final String rawMessage) {
		super(rawMessage);
		final String commandMessage = Command.strip(rawMessage);
		if (!commandMessage.startsWith(MOVE_COMMAND_PREFIX)) throw new RuntimeException("A Command Type {PATH, MOVE} needs to be supplied.");
		final String moveMessage = commandMessage.substring(MOVE_COMMAND_PREFIX.length() + 1);
		String data[] = moveMessage.split(SEPARATOR);
		if (data.length < 3) {
			System.err.println(rawMessage);
			throw new RuntimeException("A MOVE Command Message must have 3 arguments: latitude, longitude, radius.");
		}
		latitude  = Double.parseDouble(data[0]);
		longitude = Double.parseDouble(data[1]);
		radius    = Double.parseDouble(data[2]);
	}

}
