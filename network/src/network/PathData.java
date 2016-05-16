package network;

public class PathData extends Data {
	
	public final static String PATH_DATA_PREFIX = "PATH";
	
	/**
	 * Predicted time until destination in seconds.
	 */
	public final double eta;
	public final String pathCommandID;
	
	public PathData(String id, java.time.LocalDateTime timestamp, String pathCommandID, double eta) {
		super(id, timestamp);
		this.eta = eta;
		this.pathCommandID = pathCommandID;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(PATH_DATA_PREFIX); sb.append(SEPARATOR);
		sb.append(pathCommandID); sb.append(SEPARATOR);
		sb.append(eta);
		return sb.toString();
	}
	
	public PathData(final String rawMessage) {
		super(rawMessage);
		final String message = super.strip(rawMessage);
		if (!message.startsWith(PATH_DATA_PREFIX)) throw new RuntimeException("A Data Type {SCAN, STATUS, PATH} needs to be supplied.");
		final String scanMessage = message.substring(PATH_DATA_PREFIX.length() + 1);
		String data[] = scanMessage.split(SEPARATOR);
		if (data.length < 2) {
			System.err.println(rawMessage);
			throw new RuntimeException("A Path Data reply must have a path command ID, and an eta to the destination.");
		}
		pathCommandID = data[0];
		eta = Double.parseDouble(data[1]);
	}
	
}
