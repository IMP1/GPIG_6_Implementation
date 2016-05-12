package network;

public class StatusData extends Data {

	public final static String STATUS_DATA_PREFIX = "STATUS";
	
	public final String latitude;
	public final String longitude;
	public final String batteryStatus;
	public final String status;
	
	public StatusData(String id, String timestamp, String latitude, String longitude, String batteryStatus, String status) {
		super(id, timestamp);
		this.latitude = latitude;
		this.longitude = longitude;
		this.batteryStatus = batteryStatus;
		this.status = status;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(STATUS_DATA_PREFIX); sb.append(SEPARATOR);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(batteryStatus);
		return sb.toString();
	}
	
	public StatusData(final String rawMessage) {
		super(rawMessage);
		final String message = super.strip(rawMessage);
		if (!message.startsWith(STATUS_DATA_PREFIX)) throw new RuntimeException("A Data Type {SCAN, STATUS, PATH} needs to be supplied.");
		final String statusMessage = message.substring(STATUS_DATA_PREFIX.length() + 1);
		String data[] = statusMessage.split(SEPARATOR);
		if (data.length != 4) {
			System.err.println(rawMessage);
			throw new RuntimeException("A STATUS Data Message must have 4 arguments: latitude, longitude, battery status, drone state.");
		}
		// TODO: add current path as an additional argument
		latitude = data[0];
		longitude = data[1];
		batteryStatus = data[2];
		status = data[3];
	}

}
