package datastore;

import java.time.LocalDateTime;

public class Scan {
	private double locLat;
	private double locLong;
	private double depth;
	private double flowRate;
	private double[] distanceReadings;
	public LocalDateTime received;
	
	public Scan(double latitude, double longitude, double depth2, double flowRate2, double[] distanceReadings2, LocalDateTime received) {
		this.locLat = latitude;
		this.locLong = longitude;
		this.depth = depth2;
		this.flowRate = flowRate2;
		this.distanceReadings = distanceReadings2;
		this.received = received;
	}

}
