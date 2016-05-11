package network;

public class ScanData extends Message {

	protected final static String PREFIX = "DATA";
	private final static String DISTANCE_SEPARATOR = ",";

	
	String latitude;
	String longitude;
	String depth;
	String flowRate;
	String[] distanceReadings;
	
	public ScanData(String id, String timestamp, String latitude, String longitude, String depth, String flowRate, String[] distanceReadings) {
		super(id, timestamp);
		this.latitude = latitude;
		this.longitude = longitude;
		this.depth = depth;
		this.flowRate = flowRate;
		this.distanceReadings = distanceReadings;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id); sb.append(SEPARATOR);
		sb.append(timestamp); sb.append(SEPARATOR);
		sb.append(PREFIX); sb.append(SEPARATOR);
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
	
	public ScanData(String message) {
		message = setup(this, message);
		if (!message.startsWith(PREFIX)) throw new RuntimeException();
		message = message.substring(PREFIX.length() + 1);
		String data[] = message.split(SEPARATOR);
		if (data.length != 5) throw new RuntimeException();
		latitude = data[0];
		longitude = data[1];
		depth = data[2];
		flowRate = data[3];
		distanceReadings = data[4].split(DISTANCE_SEPARATOR);
	}

}
