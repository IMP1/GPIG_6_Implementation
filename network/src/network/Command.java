package network;

public class Command extends Message {
	
	protected final static String PREFIX = "COMMAND";
	private final static String MOVETO = "MOVETO";
	
	String latitude;
	String longitude;
	String radius;
	
	public Command(String id, String timestamp, String latitude, String longitude, String radius) {
		super(id, timestamp);
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id); sb.append(SEPARATOR);
		sb.append(timestamp); sb.append(SEPARATOR);
		sb.append(PREFIX); sb.append(SEPARATOR);
		sb.append(MOVETO); sb.append(SEPARATOR);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(radius);
		sb.append("\n");
		return sb.toString();
	}
	
	public Command(String message) {
		message = setup(this, message);
		if (!message.startsWith(PREFIX)) throw new RuntimeException();
		message = message.substring(PREFIX.length() + 1);
		if (!message.startsWith(MOVETO)) throw new RuntimeException();
		message = message.substring(MOVETO.length() + 1);
		String data[] = message.split(SEPARATOR);
		if (data.length != 3) throw new RuntimeException();
		latitude = data[0];
		longitude = data[1];
		radius = data[2];
	}

}
