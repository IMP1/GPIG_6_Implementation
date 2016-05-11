package network;

public class ScanData extends Message {

	protected final static String PREFIX = "DATA:";
	private final static String SEPARATOR = ", "; // Comma
	private final static String DISTANCE_SEPARATOR = "ڪ"; // Arabic Swash Kaf
	
	String latitude;
	String longitude;
	String depth;
	String flowRate;
	String[] distanceReadings;
	
	public ScanData(String latitude, String longitude, String depth, String flowRate, String[] distanceReadings) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.depth = depth;
		this.flowRate = flowRate;
		this.distanceReadings = distanceReadings;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(depth); sb.append(SEPARATOR);
		sb.append(flowRate); sb.append(SEPARATOR);
		for (String distance : distanceReadings) {
			sb.append(distance); sb.append(DISTANCE_SEPARATOR);
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public static ScanData fromString(String message) {
		if (!message.startsWith(PREFIX)) throw new RuntimeException();
		message = message.substring(PREFIX.length());
		String data[] = message.split(SEPARATOR);
		if (data.length != 5) throw new RuntimeException();
		String latitude = data[0];
		String longitude = data[1];
		String depth = data[2];
		String flowRate = data[3];
		String[] distances = data[4].split(DISTANCE_SEPARATOR);
		return new ScanData(latitude, longitude, depth, flowRate, distances);
	}

}
