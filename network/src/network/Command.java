package network;

public class Command extends Message {
	
	protected final static String PREFIX = "COMMAND:";
	private final static String MOVETO = "MOVETO:";
	private final static String SEPARATOR = ", ";
	
	String latitude;
	String longitude;
	String radius;
	
	public Command(String latitude, String longitude, String radius) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append(MOVETO);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(radius);
		sb.append("\n");
		return sb.toString();
	}
	
	public static Command fromString(String message) {
		if (!message.startsWith(PREFIX)) throw new RuntimeException();
		message = message.substring(PREFIX.length());
		if (!message.startsWith(MOVETO)) throw new RuntimeException();
		message = message.substring(MOVETO.length());
		String data[] = message.split(SEPARATOR);
		if (data.length != 3) throw new RuntimeException();
		String latitude = data[0];
		String longitude = data[1];
		String radius = data[2];
		return new Command(latitude, longitude, radius);
	}

}
