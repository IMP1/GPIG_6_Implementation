package datastore;

public class Drone {
	private String batteryLevel;
	private String locLat;
	private String locLong;
	private String status;
	private String timestamp;
	
	public Drone(String batteryLevel, String locLat, String locLong, String status, String timestamp) {
		super();
		this.batteryLevel = batteryLevel;
		this.locLat = locLat;
		this.locLong = locLong;
		this.status = status;
		this.timestamp = timestamp;
	}
	
	public String getBatteryLevel() {
		return batteryLevel;
	}
	public void setBatteryLevel(String batteryStatus) {
		this.batteryLevel = batteryStatus;
	}
	public String getLocLat() {
		return locLat;
	}
	public void setLocLat(String locLat) {
		this.locLat = locLat;
	}
	public String getLocLong() {
		return locLong;
	}
	public void setLocLong(String locLong) {
		this.locLong = locLong;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
