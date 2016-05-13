package datastore;

import java.time.LocalDateTime;

public class Drone {
	private double batteryLevel;
	private double locLat;
	private double locLong;
	private String status;
	private LocalDateTime timestamp;
	
	public Drone(double batteryLevel, double locLat, double locLong, String status, LocalDateTime timestamp) {
		super();
		this.batteryLevel = batteryLevel;
		this.locLat = locLat;
		this.locLong = locLong;
		this.status = status.trim();
		this.timestamp = timestamp;
	}
	
	public double getBatteryLevel() {
		return batteryLevel;
	}
	public void setBatteryLevel(double batteryStatus) {
		this.batteryLevel = batteryStatus;
	}
	public double getLocLat() {
		return locLat;
	}
	public void setLocLat(double latitude) {
		this.locLat = latitude;
	}
	public double getLocLong() {
		return locLong;
	}
	public void setLocLong(double locLong) {
		this.locLong = locLong;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

}
