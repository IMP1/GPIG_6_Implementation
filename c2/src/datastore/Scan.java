package datastore;

import java.time.LocalDateTime;

public class Scan {
	public double locLat;
	public double locLong;
	public double depth;
	public double flowRate;
	public double[] distanceReadings;
	public double[] rawDistanceReadings;
	public LocalDateTime received;
	
	public Scan(double latitude, double longitude, double depth2, double flowRate2, double[] distanceReadings2, double[] rawDistanceReadings, LocalDateTime received) {
		this.locLat = latitude;
		this.locLong = longitude;
		this.depth = depth2;
		this.flowRate = flowRate2;
		this.distanceReadings = distanceReadings2;
		this.rawDistanceReadings = rawDistanceReadings;
		this.received = received;
	}

}
