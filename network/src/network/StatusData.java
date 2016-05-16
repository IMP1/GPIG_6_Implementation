package network;

public class StatusData extends Data {

	public final static String STATUS_DATA_PREFIX = "STATUS";
	public final static String POINT_SEPARATOR = ",";
	
	public enum DroneState {
		IDLE,
		MOVING,
		SCANNING,
		BATTERY_LOW
	}
	
	public final double latitude;
	public final double longitude;
	public final double batteryStatus;
	public final DroneState status;
	public final double[] currentPath;
	
	public StatusData(String id, java.time.LocalDateTime timestamp, double latitude, double longitude, 
			          double batteryStatus, DroneState status, double[] currentPath) {
		super(id, timestamp);
		this.latitude      = latitude;
		this.longitude     = longitude;
		this.batteryStatus = batteryStatus;
		this.status        = status;
		this.currentPath   = currentPath;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(STATUS_DATA_PREFIX); sb.append(SEPARATOR);
		sb.append(latitude); sb.append(SEPARATOR);
		sb.append(longitude); sb.append(SEPARATOR);
		sb.append(batteryStatus); sb.append(SEPARATOR);
		sb.append(status.name()); sb.append(SEPARATOR);
		for (double point : currentPath) {
			sb.append(point); sb.append(POINT_SEPARATOR);
		}
		return sb.toString();
	}
	
	public StatusData(final String rawMessage) {
		super(rawMessage);
		final String message = super.strip(rawMessage);
		if (!message.startsWith(STATUS_DATA_PREFIX)) throw new RuntimeException("A Data Type {SCAN, STATUS, PATH} needs to be supplied.");
		final String statusMessage = message.substring(STATUS_DATA_PREFIX.length() + 1);
		String data[] = statusMessage.split(SEPARATOR);
		if (data.length < 5) {
			System.err.println(rawMessage);
			throw new RuntimeException("A STATUS Data Message must have 5 arguments: latitude, longitude, battery status, drone state, current path.");
		}
		latitude      = Double.parseDouble(data[0]);
		longitude     = Double.parseDouble(data[1]);
		batteryStatus = Double.parseDouble(data[2]);
		status        = DroneState.valueOf(data[3]);
		String[] pathData = data[4].split(POINT_SEPARATOR);
		currentPath = new double[pathData.length];
		for (int i = 0; i < currentPath.length; i ++) {
			currentPath[i] = Double.parseDouble(pathData[i]);
		}
	}

}
