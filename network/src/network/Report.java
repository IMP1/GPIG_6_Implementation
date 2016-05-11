package network;

public class Report extends Message {

	protected final static String PREFIX = "INFO:";
	private final static String SEPARATOR = ", ";
	
	String latitude;
	String longitude;
	String batteryStatus;
	String status;
	
	public Report(String latitude, String longitude, String batteryStatus, String status) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.batteryStatus = batteryStatus;
		this.status = status;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(batteryStatus);
		sb.append("\n");
		return sb.toString();
	}
	
	public static Report fromString(String message) {
		if (!message.startsWith(PREFIX)) throw new RuntimeException();
		message = message.substring(PREFIX.length());
		String data[] = message.split(SEPARATOR);
		if (data.length != 4) throw new RuntimeException();
		String latitude = data[0];
		String longitude = data[1];
		String batteryStatus = data[2];
		String status = data[4];
		return new Report(latitude, longitude, batteryStatus, status);
	}

}
