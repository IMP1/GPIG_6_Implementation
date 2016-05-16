package network;

public class ScanData extends Data {

	public final static String SCAN_DATA_PREFIX = "SCAN";
	public final static String DISTANCE_SEPARATOR = ",";
	
	public final double latitude;
	public final double longitude;
	public final double depth;
	public final double flowRate;
	public final double[] distanceReadings;
	
	public ScanData(String id, java.time.LocalDateTime timestamp, double latitude, double longitude, double depth, double flowRate, double[] distanceReadings) {
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
		for (double distance : distanceReadings) {
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
		if (data.length < 5) {
			System.err.println(rawMessage);
			throw new RuntimeException("A SCAN Data Message must have 5 arguments: latitude, longitude, depth reading, flow rate, distance readings.");
		}
		latitude  = Double.parseDouble(data[0]);
		longitude = Double.parseDouble(data[1]);
		depth     = Double.parseDouble(data[2]);
		flowRate  = Double.parseDouble(data[3]);
		String[] distanceData = data[4].split(DISTANCE_SEPARATOR);
		distanceReadings = new double[distanceData.length];
		for (int i = 0; i < distanceReadings.length; i ++) {
			distanceReadings[i] = Double.parseDouble(distanceData[i]);
		}
	}

}
