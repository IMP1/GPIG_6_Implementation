package datastore;

import java.time.LocalDateTime;

import network.StatusData.DroneState;

public class Drone {
	private double batteryLevel;
	private double locLat;
	private double locLong;
	private DroneState status;
	private LocalDateTime timestamp;
	private boolean locked;
	
	public Drone(double batteryLevel, double locLat, double locLong, DroneState status, LocalDateTime timestamp) {
		super();
		this.batteryLevel = batteryLevel;
		this.locLat = locLat;
		this.locLong = locLong;
		this.status = status;
		this.timestamp = timestamp;
		this.locked = false;
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
	public DroneState getStatus() {
		return status;
	}
	public void setStatus(DroneState status) {
		if(status == DroneState.MOVING){
			this.locked = false;
		}
		this.status = status;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setLock(){
		this.locked = true;
	}
	public boolean isLocked(){
		return this.locked;
	}

}
