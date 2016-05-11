package network;

import java.util.Arrays;

public class Report extends Message {

	protected final static String PREFIX = "INFO";
	
	public String latitude;
	public String longitude;
	public String batteryStatus;
	public String status;
	
	public Report(String id, String timestamp, String latitude, String longitude, String batteryStatus, String status) {
		super(id, timestamp);
		this.latitude = latitude;
		this.longitude = longitude;
		this.batteryStatus = batteryStatus;
		this.status = status;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id); sb.append(SEPARATOR);
		sb.append(timestamp); sb.append(SEPARATOR);
		sb.append(PREFIX); sb.append(SEPARATOR);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(batteryStatus);
		sb.append("\n");
		return sb.toString();
	}
	
	public Report(String message) {
		message = setup(this, message);
		if (!message.startsWith(PREFIX)) throw new RuntimeException();
		message = message.substring(PREFIX.length() + 1);
		String data[] = message.split(SEPARATOR);
		System.out.println(message);
		System.out.println(Arrays.toString(data));
		// Starting at 2 because of ID and timestamp.
		if (data.length != 4) throw new RuntimeException();
		latitude = data[0];
		longitude = data[1];
		batteryStatus = data[2];
		status = data[3];
	}

}
