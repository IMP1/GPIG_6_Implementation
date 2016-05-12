package network;

public class ScanData extends Data {

	public final static String SCAN_DATA_PREFIX = "SCAN";
	public final static String DISTANCE_SEPARATOR = ",";
	
	public final String latitude;
	public final String longitude;
	public final String depth;
	public final String flowRate;
	public final String[] distanceReadings;
	
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
		sb.append(super.toString());
		sb.append(SCAN_DATA_PREFIX); sb.append(SEPARATOR);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(depth); sb.append(SEPARATOR);
		sb.append(flowRate); sb.append(SEPARATOR);
		for (String distance : distanceReadings) {
			sb.append(distance); sb.append(DISTANCE_SEPARATOR);
		}
		return sb.toString();
	}
	
	public ScanData(final String rawMessage) {
		super(rawMessage);
		final String message = super.strip(rawMessage);
		if (!message.startsWith(SCAN_DATA_PREFIX)) throw new RuntimeException("A Data Type {SCAN, STATUS, PATH} needs to be supplied.");
		final String scanMessage = message.substring(SCAN_DATA_PREFIX.length() + 1);
		String data[] = scanMessage.split(SEPARATOR);
		if (data.length != 5) {
			System.err.println(rawMessage);
			throw new RuntimeException("A SCAN Data Message must have 5 arguments: latitude, longitude, depth reading, flow rate, distance readings.");
		}
		latitude         = data[0];
		longitude        = data[1];
		depth            = data[2];
		flowRate         = data[3];
		distanceReadings = data[4].split(DISTANCE_SEPARATOR);
	}

}
