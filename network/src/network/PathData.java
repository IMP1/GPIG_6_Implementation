package network;

public class PathData extends Data {
	
	public final static String PATH_DATA_PREFIX = "PATH";
	public final static String POINT_SEPARATOR = ",";
	
	public final String[] points;
	
	public PathData(String id, String timestamp, String[] points) {
		super(id, timestamp);
		this.points = points;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(PATH_DATA_PREFIX); sb.append(SEPARATOR);
		for (String distance : points) {
			sb.append(distance); sb.append(POINT_SEPARATOR);
		}
		return sb.toString();
	}
	
	public PathData(final String rawMessage) {
		super(rawMessage);
		final String message = super.strip(rawMessage);
		if (!message.startsWith(PATH_DATA_PREFIX)) throw new RuntimeException("A Data Type {SCAN, STATUS, PATH} needs to be supplied.");
		final String scanMessage = message.substring(PATH_DATA_PREFIX.length() + 1);
		String data[] = scanMessage.split(SEPARATOR);
		if (data.length != 1) {
			System.err.println(rawMessage);
			throw new RuntimeException("A SCAN Data Message must have 5 arguments: latitude, longitude, depth reading, flow rate, distance readings.");
		}
		points = data[0].split(POINT_SEPARATOR);
	}
	
}
